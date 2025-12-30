package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.data.models.item.Item;

import java.math.BigDecimal;

public record ItemDto(
        Long id,
        TipoItem tipo,
        String nombre,
        String descripcion,
        String codigoItem,
        BigDecimal stockTotal,
        BigDecimal stockDisponible,
        boolean enabled,
        String observaciones
) {

    public static ItemDto from(Item item) {
        return new ItemDto(
                item.getId(),
                item.getTipo(),
                item.getNombre(),
                item.getDescripcion(),
                item.getCodigoItem(),
                item.getStockTotal(),
                item.getStockDisponible(),
                item.isEnabled(),
                item.getObservaciones()
        );
    }
}
