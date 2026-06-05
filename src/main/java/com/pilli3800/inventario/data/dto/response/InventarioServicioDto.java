package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.InventarioServicio;

public record InventarioServicioDto(
        Long id,
        String codigoServicio,
        String codigoItem,
        String nombreItem,
        String tipoItem,
        Long stockActual,
        boolean stockCritico
) {

    public static InventarioServicioDto from(InventarioServicio entity) {
        Long stockMinimo = entity.getItem().getStockMinimo();
        long stockActual = entity.getStockActual() != null ? entity.getStockActual() : 0L;
        return new InventarioServicioDto(
                entity.getId(),
                entity.getServicio().getCodigo(),
                entity.getItem().getCodigoItem(),
                entity.getItem().getNombre(),
                entity.getItem().getTipo().name(),
                stockActual,
                stockMinimo != null && stockActual <= stockMinimo
        );
    }
}
