package com.pilli3800.inventario.data.dto.response.ml;

import java.util.List;

public record ConsumoAnomaliaResponse(
        ConsumoCriterioDto criterio,
        ConsumoPeriodoAnalizadoDto periodoAnalizado,
        Integer totalRegistros,
        Integer totalAnomalias,
        List<ConsumoAnomaliaDto> resultados
) {
}
