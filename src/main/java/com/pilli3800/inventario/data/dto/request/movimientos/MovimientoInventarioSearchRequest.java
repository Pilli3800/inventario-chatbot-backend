package com.pilli3800.inventario.data.dto.request.movimientos;

import com.pilli3800.inventario.data.models.enums.TipoMovimiento;

import java.time.LocalDate;

public record MovimientoInventarioSearchRequest(
        String codigoItem,
        String sedeOrigen,
        String sedeDestino,
        TipoMovimiento tipoMovimiento,
        String usuario,
        String codigoCuadrilla,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {}
