package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.TipoMovimiento;

import java.time.LocalDate;

public record MovimientoHistoricoDashboardFechaTipoDto(
        LocalDate fecha,
        TipoMovimiento tipoMovimiento,
        Long total
) {
}
