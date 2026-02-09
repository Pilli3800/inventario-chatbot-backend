package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.solicituditems.SolicitudItemsDetalle;

public record SolicitudItemsDetalleDto(
        String codigoItem,
        String nombreItem,
        Long cantidad
) {
    public static SolicitudItemsDetalleDto from(SolicitudItemsDetalle detalle) {
        return new SolicitudItemsDetalleDto(
                detalle.getItem().getCodigoItem(),
                detalle.getItem().getNombre(),
                detalle.getCantidad()
        );
    }
}

