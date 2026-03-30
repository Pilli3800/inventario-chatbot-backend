package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.valesalida.ValeSalidaDetalle;

public record ValeSalidaDetalleDto(
        Integer numeroItem,
        String codigoItem,
        String nombreItem,
        String descripcionItem,
        Long cantidadEntregada,
        String ordenTrabajo,
        String estadoObservacion
) {
    public static ValeSalidaDetalleDto from(ValeSalidaDetalle detalle) {
        return new ValeSalidaDetalleDto(
                detalle.getNumeroItem(),
                detalle.getCodigoItemSnapshot(),
                detalle.getNombreItemSnapshot(),
                detalle.getDescripcionItemSnapshot(),
                detalle.getCantidadEntregada(),
                detalle.getOrdenTrabajo(),
                detalle.getEstadoObservacion()
        );
    }
}
