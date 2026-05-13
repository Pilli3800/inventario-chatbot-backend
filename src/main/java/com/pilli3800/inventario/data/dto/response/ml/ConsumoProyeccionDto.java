package com.pilli3800.inventario.data.dto.response.ml;

public record ConsumoProyeccionDto(
        String fecha,
        Double consumoEstimado,
        String metodo,
        String explicacion
) {
}
