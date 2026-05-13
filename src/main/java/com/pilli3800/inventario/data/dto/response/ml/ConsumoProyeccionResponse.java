package com.pilli3800.inventario.data.dto.response.ml;

import java.util.List;

public record ConsumoProyeccionResponse(
        ConsumoCriterioDto criterio,
        ConsumoPeriodoAnalizadoDto periodoAnalizado,
        Resumen resumen,
        List<ConsumoProyeccionDto> resultados,
        String explicacionGeneral
) {

    public record Resumen(
            Double consumoPromedioDiario,
            Double desviacionStdDiaria
    ) {
    }
}
