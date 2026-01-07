package com.pilli3800.inventario.data.dto.response;

public record StockMovidoPorItemDto(
        String codigoItem,
        String nombreItem,
        Long totalMovido
) {}
