package com.pilli3800.inventario.data.dto.response;

import java.time.LocalDate;

public record MovimientoHistoricoDashboardFechaDto(
        LocalDate fecha,
        Long compra,
        Long entrada,
        Long salida,
        Long salidaCuadrilla,
        Long devolucion,
        Long transferencia,
        Long transferenciaServicio,
        Long retornoASede
) {
}
