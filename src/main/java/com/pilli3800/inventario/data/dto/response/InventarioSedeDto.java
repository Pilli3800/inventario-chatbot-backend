package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.InventarioSede;

public record InventarioSedeDto(
        Long id,
        String sedeCodigo,
        String codigoItem,
        String nombreItem,
        String tipoItem,
        Long stock,
        boolean stockCritico
) {

    public static InventarioSedeDto from(InventarioSede entity) {
        Long stockMinimo = entity.getItem().getStockMinimo();
        long stock = entity.getStock() != null ? entity.getStock() : 0L;
        return new InventarioSedeDto(
                entity.getId(),
                entity.getSede().getCodigo(),
                entity.getItem().getCodigoItem(),
                entity.getItem().getNombre(),
                entity.getItem().getTipo().name(),
                stock,
                stockMinimo != null && stock <= stockMinimo
        );
    }
}
