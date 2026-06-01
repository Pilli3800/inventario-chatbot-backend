package com.pilli3800.inventario.data.dto.response;

import java.util.List;

public record MovimientoHistoricoDashboardDto(
        Long total,
        Long compra,
        Long entrada,
        Long salida,
        Long salidaCuadrilla,
        Long devolucion,
        Long ajuste,
        Long transferencia,
        Long transferenciaServicio,
        Long retornoASede,
        Long movimientosTrazables,
        Double nivelTrazabilidad,
        List<MovimientoHistoricoDashboardFechaDto> porFecha
) {
}
