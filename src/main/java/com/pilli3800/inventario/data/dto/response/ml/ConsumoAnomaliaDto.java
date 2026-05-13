package com.pilli3800.inventario.data.dto.response.ml;

public record ConsumoAnomaliaDto(
        String cuadrillaCodigo,
        String itemCodigo,
        String itemNombre,
        Double consumoActual,
        Double consumoPromedio,
        Double desviacionStd,
        Double zScore,
        Double anomalyScore,
        Boolean isAnomaly,
        String explicacion
) {
}
