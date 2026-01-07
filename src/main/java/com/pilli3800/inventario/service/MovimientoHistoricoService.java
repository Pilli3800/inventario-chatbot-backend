package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioSearchRequest;
import com.pilli3800.inventario.data.dto.response.StockMovidoPorItemDto;
import com.pilli3800.inventario.data.dto.response.MovimientoInventarioDto;
import com.pilli3800.inventario.data.models.MovimientoInventario;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoHistoricoService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public Page<MovimientoInventarioDto> getMovimientos(MovimientoInventarioSearchRequest request, Pageable pageable) {

        Specification<MovimientoInventario> spec =
                MovimientoInventarioSpecifications.search(request);

        Page<MovimientoInventario> page =
                movimientoInventarioRepository.findAll(spec, pageable);

        return page.map(MovimientoInventarioDto::from);
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
            LocalDate fecha,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer mes,
            Integer anio
    ) {
        return movimientoInventarioRepository.obtenerStockMovidoPorItem(
                fecha, fechaDesde, fechaHasta, mes, anio
        );
    }

    public StockMovidoPorItemDto obtenerStockMovidoPorItem(
            String codigoItem,
            LocalDate fecha,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer mes,
            Integer anio
    ) {
        return movimientoInventarioRepository.obtenerStockMovidoPorItem(
                codigoItem, fecha, fechaDesde, fechaHasta, mes, anio
        );
    }

}