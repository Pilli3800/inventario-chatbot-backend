package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioSearchRequest;
import com.pilli3800.inventario.data.dto.response.StockMovidoPorItemDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.MovimientoInventarioDto;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.service.MovimientoHistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos/historico")
@RequiredArgsConstructor
public class MovimientoHistoricoController {

    private final MovimientoHistoricoService movimientoHistoricoService;

    @GetMapping("/{idMovimiento}")
    @ResponseStatus(HttpStatus.OK)
    public MovimientoInventarioDto getMovimiento(
            @PathVariable Long idMovimiento,
            Authentication authentication
    ) {

        boolean esJefeCuadrilla = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JEFE_CUADRILLA"));

        String usuario = authentication.getName();

        return movimientoHistoricoService.getMovimiento(idMovimiento, esJefeCuadrilla, usuario);
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<MovimientoInventarioDto> getHistorico(
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) String sedeOrigen,
            @RequestParam(required = false) String sedeDestino,
            @RequestParam(required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @PageableDefault(page = 0, size = 10, sort = "fechaMovimiento") Pageable pageable,
            Authentication authentication
    ) {

        boolean esJefeCuadrilla = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JEFE_CUADRILLA"));

        String usuarioFinal = esJefeCuadrilla
                ? authentication.getName()
                : usuario;

        MovimientoInventarioSearchRequest request =
                new MovimientoInventarioSearchRequest(
                        codigoItem,
                        sedeOrigen,
                        sedeDestino,
                        tipoMovimiento,
                        usuarioFinal,
                        codigoCuadrilla,
                        fechaDesde,
                        fechaHasta
                );

        Page<MovimientoInventarioDto> page =
                movimientoHistoricoService.getMovimientos(request, pageable);

        return PageResponse.from(page);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportHistoricoExcel(
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) String sedeOrigen,
            @RequestParam(required = false) String sedeDestino,
            @RequestParam(required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta
    ) throws IOException {

        MovimientoInventarioSearchRequest request =
                new MovimientoInventarioSearchRequest(
                        codigoItem,
                        sedeOrigen,
                        sedeDestino,
                        tipoMovimiento,
                        usuario,
                        codigoCuadrilla,
                        fechaDesde,
                        fechaHasta
                );

        byte[] excel = movimientoHistoricoService.exportHistoricoToExcel(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=historico_movimientos.xlsx")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

    @GetMapping("/stock-movido")
    @ResponseStatus(HttpStatus.OK)
    public List<StockMovidoPorItemDto> getStockMovidoPorItem(

            @RequestParam(required = false)
            LocalDate fecha,

            @RequestParam(required = false)
            LocalDate fechaDesde,

            @RequestParam(required = false)
            LocalDate fechaHasta,

            @RequestParam(required = false)
            Integer mes,

            @RequestParam(required = false)
            Integer anio
    ) {
        return movimientoHistoricoService.obtenerStockMovidoPorItem(
                fecha,
                fechaDesde,
                fechaHasta,
                mes,
                anio
        );
    }

    @GetMapping("/stock-movido/item/{codigoItem}")
    @ResponseStatus(HttpStatus.OK)
    public StockMovidoPorItemDto getStockMovidoPorItemPorItem(
            @PathVariable String codigoItem,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio
    ) {
        return movimientoHistoricoService.obtenerStockMovidoPorItem(
                codigoItem, fecha, fechaDesde, fechaHasta, mes, anio
        );
    }

}
