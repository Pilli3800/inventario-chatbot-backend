package com.pilli3800.inventario.data.dto.response;

import java.util.List;

public record MovimientoHistoricoDashboardDto(
        Long total,
        Long compra,
        Long entrada,
        Long salida,
        Long salidaCuadrilla,
        Long devolucion,
        Long transferencia,
        Long transferenciaServicio,
        Long retornoASede,
        List<MovimientoHistoricoDashboardFechaDto> porFecha
) {
}
