package com.pilli3800.inventario.data.dto.response.ml;

import java.util.List;

public record ConsumoEvolucionResponse(
        ConsumoCriterioDto criterio,
        ConsumoPeriodoAnalizadoDto periodoAnalizado,
        Resumen resumen,
        List<ConsumoEvolucionDto> resultados
) {

    public record Resumen(
            Double consumoTotal,
            Double consumoPromedioDiario,
            Double desviacionStdDiaria,
            Integer eventosDestacados
    ) {
    }
}
