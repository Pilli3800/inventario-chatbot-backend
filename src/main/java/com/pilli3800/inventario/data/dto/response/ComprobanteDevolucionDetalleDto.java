package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucionDetalle;

public record ComprobanteDevolucionDetalleDto(
        Integer numeroItem,
        String codigoItem,
        String nombreItem,
        String descripcionItem,
        Long cantidadDevuelta,
        String estadoObservacion
) {
    public static ComprobanteDevolucionDetalleDto from(ComprobanteDevolucionDetalle detalle) {
        return new ComprobanteDevolucionDetalleDto(
                detalle.getNumeroItem(),
                detalle.getCodigoItemSnapshot(),
                detalle.getNombreItemSnapshot(),
                detalle.getDescripcionItemSnapshot(),
                detalle.getCantidadDevuelta(),
                detalle.getEstadoObservacion()
        );
    }
}
