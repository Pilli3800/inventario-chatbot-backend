package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.InventarioServicio;

public record InventarioServicioDto(
        Long id,
        String codigoServicio,
        String codigoItem,
        String nombreItem,
        String tipoItem,
        Long stockActual
) {

    public static InventarioServicioDto from(InventarioServicio entity) {
        return new InventarioServicioDto(
                entity.getId(),
                entity.getServicio().getCodigo(),
                entity.getItem().getCodigoItem(),
                entity.getItem().getNombre(),
                entity.getItem().getTipo().name(),
                entity.getStockActual()
        );
    }
}
