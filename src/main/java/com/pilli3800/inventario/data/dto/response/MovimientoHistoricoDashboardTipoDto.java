package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.TipoMovimiento;

public record MovimientoHistoricoDashboardTipoDto(
        TipoMovimiento tipoMovimiento,
        Long total
) {
}
