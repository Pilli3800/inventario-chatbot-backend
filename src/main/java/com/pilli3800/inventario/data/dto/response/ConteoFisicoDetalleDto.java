package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.conteofisico.ConteoFisicoDetalle;

public record ConteoFisicoDetalleDto(
        String codigoItem,
        String nombreItem,
        Long stockSistema,
        Long cantidadFisica,
        Long diferencia,
        String observacion
) {
    public static ConteoFisicoDetalleDto from(ConteoFisicoDetalle detalle) {
        return new ConteoFisicoDetalleDto(
                detalle.getItem().getCodigoItem(),
                detalle.getItem().getNombre(),
                detalle.getStockSistema(),
                detalle.getCantidadFisica(),
                detalle.getDiferencia(),
                detalle.getObservacion()
        );
    }
}
