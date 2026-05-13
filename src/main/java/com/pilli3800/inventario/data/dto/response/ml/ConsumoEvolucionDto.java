package com.pilli3800.inventario.data.dto.response.ml;

public record ConsumoEvolucionDto(
        String fecha,
        Double consumoDiario,
        Double tendencia,
        Double zScore,
        Boolean eventoDestacado,
        String explicacion
) {
}
