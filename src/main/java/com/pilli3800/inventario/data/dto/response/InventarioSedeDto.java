package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.InventarioSede;

public record InventarioSedeDto(
        Long id,
        String sedeCodigo,
        String codigoItem,
        String nombreItem,
        String tipoItem,
        Long stock
) {

    public static InventarioSedeDto from(InventarioSede entity) {
        return new InventarioSedeDto(
                entity.getId(),
                entity.getSede().getCodigo(),
                entity.getItem().getCodigoItem(),
                entity.getItem().getNombre(),
                entity.getItem().getTipo().name(),
                entity.getStock()
        );
    }
}