package com.pilli3800.inventario.data.dto.response.ml;

public record ConsumoCriterioDto(
        String metodo,
        Integer diasPeriodo,
        Integer periodosHistorial,
        Double zScoreThreshold,
        Integer diasHist,
        Integer diasFuturo,
        String metodoTendencia,
        String metodoEvento
) {
}
