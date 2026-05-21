package com.pilli3800.inventario.data.dto.response;

public record ItemMovimientosCantidadDto(
        String codigoItem,
        String nombreItem,
        Long cantidadMovimientos
) {}
