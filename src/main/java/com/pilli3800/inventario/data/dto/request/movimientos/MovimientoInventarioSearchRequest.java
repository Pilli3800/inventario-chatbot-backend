package com.pilli3800.inventario.data.dto.request.movimientos;

import com.pilli3800.inventario.data.models.enums.TipoMovimiento;

import java.time.LocalDate;

public record MovimientoInventarioSearchRequest(
        String codigoItem,
        String sedeCodigo,
        TipoMovimiento tipoMovimiento,
        String usuario,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) { }
