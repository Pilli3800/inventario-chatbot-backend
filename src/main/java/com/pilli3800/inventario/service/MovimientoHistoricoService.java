package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioSearchRequest;
import com.pilli3800.inventario.data.dto.response.ItemMovimientosCantidadDto;
import com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardDto;
import com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardFechaTipoDto;
import com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardFechaDto;
import com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardTipoDto;
import com.pilli3800.inventario.data.dto.response.MovimientoInventarioDto;
import com.pilli3800.inventario.data.dto.response.StockMovidoPorItemDto;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.repository.CuadrillaRepository;
import com.pilli3800.inventario.repository.MovimientoInventarioRepository;
import com.pilli3800.inventario.specifications.MovimientoInventarioSpecifications;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class MovimientoHistoricoService {

    private static final LocalDateTime FECHA_MINIMA = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FECHA_MAXIMA = LocalDateTime.of(3000, 1, 1, 0, 0);

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CuadrillaRepository cuadrillaRepository;

    public MovimientoInventarioDto getMovimiento(
            Long id,
            boolean esJefeCuadrilla,
            String usuarioIdent
    ) {

        MovimientoInventario movimiento = movimientoInventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));

        if (esJefeCuadrilla) {
            if (!movimiento.getUsuario().getIdentUsuario().equals(usuarioIdent)) {
                throw new RuntimeException("No tiene permiso para ver este movimiento");
            }
        }

        return MovimientoInventarioDto.from(movimiento);
    }

    public Page<MovimientoInventarioDto> getMovimientos(
            MovimientoInventarioSearchRequest request,
            Pageable pageable
    ) {

        Specification<MovimientoInventario> spec =
                MovimientoInventarioSpecifications.search(request);

        // 🔥 resolver cuadrilla SOLO AQUÍ
        if (request.codigoCuadrilla() != null && !request.codigoCuadrilla().isBlank()) {

            Cuadrilla cuadrilla = cuadrillaRepository
                    .findByCodigoCuadrilla(request.codigoCuadrilla())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No existe cuadrilla con código " + request.codigoCuadrilla()
                            )
                    );

            spec = spec.and(
                    MovimientoInventarioSpecifications.byCuadrillaId(cuadrilla.getId())
            );
        }

        return movimientoInventarioRepository
                .findAll(spec, pageable)
                .map(MovimientoInventarioDto::from);
    }

    public MovimientoHistoricoDashboardDto getDashboard(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String usuario,
            String codigoCuadrilla,
            String codigoServicio,
            boolean porFechas
    ) {
        FiltrosFecha filtros = filtrosFecha(fechaDesde, fechaHasta);
        EnumMap<TipoMovimiento, Long> porTipoMovimiento = inicializarContadorTipos();
        String usuarioFiltro = usuario != null && !usuario.isBlank() ? usuario : "%";
        String cuadrillaFiltro = codigoCuadrilla != null && !codigoCuadrilla.isBlank() ? codigoCuadrilla : "%";
        String servicioFiltro = codigoServicio != null && !codigoServicio.isBlank() ? codigoServicio : "%";

        List<MovimientoHistoricoDashboardTipoDto> filas =
                movimientoInventarioRepository.contarPorTipoMovimientoDashboard(
                filtros.desde(),
                filtros.hasta(),
                usuarioFiltro,
                cuadrillaFiltro,
                servicioFiltro
        );

        for (MovimientoHistoricoDashboardTipoDto fila : filas) {
            porTipoMovimiento.put(fila.tipoMovimiento(), fila.total());
        }

        Long total = porTipoMovimiento.values()
                .stream()
                .reduce(0L, Long::sum);

        return new MovimientoHistoricoDashboardDto(
                total,
                porTipoMovimiento.get(TipoMovimiento.COMPRA),
                porTipoMovimiento.get(TipoMovimiento.ENTRADA),
                porTipoMovimiento.get(TipoMovimiento.SALIDA),
                porTipoMovimiento.get(TipoMovimiento.SALIDA_CUADRILLA),
                porTipoMovimiento.get(TipoMovimiento.DEVOLUCION),
                porTipoMovimiento.get(TipoMovimiento.TRANSFERENCIA),
                porTipoMovimiento.get(TipoMovimiento.TRANSFERENCIA_SERVICIO),
                porTipoMovimiento.get(TipoMovimiento.RETORNO_A_SEDE),
                porFechas
                        ? obtenerDashboardPorFecha(filtros, usuarioFiltro, cuadrillaFiltro, servicioFiltro)
                        : null
        );
    }

    public byte[] exportHistoricoToExcel(MovimientoInventarioSearchRequest request) throws IOException {

        Specification<MovimientoInventario> spec =
                MovimientoInventarioSpecifications.search(request);

        List<MovimientoInventario> movimientos =
                movimientoInventarioRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Histórico Movimientos");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {
                "ID",
                "Tipo",
                "Código Item",
                "Nombre Item",
                "Cantidad",
                "Sede Origen",
                "Sede Destino",
                "Usuario",
                "Fecha Movimiento",
                "Observaciones"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (MovimientoInventario mov : movimientos) {

            MovimientoInventarioDto dto =
                    MovimientoInventarioDto.from(mov);

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(dto.id());
            row.createCell(1).setCellValue(dto.tipoMovimiento());
            row.createCell(2).setCellValue(dto.codigoItem());
            row.createCell(3).setCellValue(dto.nombreItem());
            row.createCell(4).setCellValue(dto.cantidad());
            row.createCell(5).setCellValue(dto.sedeOrigen());
            row.createCell(6).setCellValue(dto.sedeDestino());
            row.createCell(7).setCellValue(dto.usuario());
            row.createCell(8).setCellValue(dto.fechaMovimiento().toString());
            row.createCell(9).setCellValue(dto.observaciones());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public List<StockMovidoPorItemDto> obtenerStockMovidoPorItem(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        FiltrosFecha filtros = filtrosFecha(fechaDesde, fechaHasta);

        return movimientoInventarioRepository.obtenerStockMovidoPorItem(
                filtros.desde(),
                filtros.hasta()
        );
    }

    public StockMovidoPorItemDto obtenerStockMovidoPorItem(
            String codigoItem,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        FiltrosFecha filtros = filtrosFecha(fechaDesde, fechaHasta);

        return movimientoInventarioRepository.obtenerStockMovidoPorItem(
                codigoItem,
                filtros.desde(),
                filtros.hasta()
        );
    }

    public List<ItemMovimientosCantidadDto> obtenerItemsConMasMovimientos(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        FiltrosFecha filtros = filtrosFecha(fechaDesde, fechaHasta);

        return movimientoInventarioRepository.obtenerItemsConMasMovimientos(
                filtros.desde(),
                filtros.hasta()
        );
    }

    private FiltrosFecha filtrosFecha(
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        return new FiltrosFecha(
                fechaDesde != null ? fechaDesde.atStartOfDay() : FECHA_MINIMA,
                fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : FECHA_MAXIMA
        );
    }

    private EnumMap<TipoMovimiento, Long> inicializarContadorTipos() {
        EnumMap<TipoMovimiento, Long> contador =
                new EnumMap<>(TipoMovimiento.class);
        for (TipoMovimiento tipoMovimiento : TipoMovimiento.values()) {
            contador.put(tipoMovimiento, 0L);
        }
        return contador;
    }

    private List<MovimientoHistoricoDashboardFechaDto> obtenerDashboardPorFecha(
            FiltrosFecha filtros,
            String usuario,
            String codigoCuadrilla,
            String codigoServicio
    ) {
        Map<LocalDate, EnumMap<TipoMovimiento, Long>> contadoresPorFecha = new TreeMap<>();

        List<MovimientoHistoricoDashboardFechaTipoDto> filas =
                movimientoInventarioRepository.contarPorFechaYTipoMovimientoDashboard(
                filtros.desde(),
                filtros.hasta(),
                usuario,
                codigoCuadrilla,
                codigoServicio
        );

        for (MovimientoHistoricoDashboardFechaTipoDto fila : filas) {
            EnumMap<TipoMovimiento, Long> contador =
                    contadoresPorFecha.computeIfAbsent(fila.fecha(), key -> inicializarContadorTipos());
            contador.put(fila.tipoMovimiento(), fila.total());
        }

        List<MovimientoHistoricoDashboardFechaDto> resultado = new ArrayList<>();
        for (Map.Entry<LocalDate, EnumMap<TipoMovimiento, Long>> entry : contadoresPorFecha.entrySet()) {
            EnumMap<TipoMovimiento, Long> contador = entry.getValue();
            resultado.add(new MovimientoHistoricoDashboardFechaDto(
                    entry.getKey(),
                    contador.get(TipoMovimiento.COMPRA),
                    contador.get(TipoMovimiento.ENTRADA),
                    contador.get(TipoMovimiento.SALIDA),
                    contador.get(TipoMovimiento.SALIDA_CUADRILLA),
                    contador.get(TipoMovimiento.DEVOLUCION),
                    contador.get(TipoMovimiento.TRANSFERENCIA),
                    contador.get(TipoMovimiento.TRANSFERENCIA_SERVICIO),
                    contador.get(TipoMovimiento.RETORNO_A_SEDE)
            ));
        }

        return resultado;
    }

    private record FiltrosFecha(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {}

}
