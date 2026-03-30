package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.FacturaCompra;

import java.time.LocalDate;

public record FacturaCompraDto(
        Long id,
        String codigoProveedor,
        String rucProveedor,
        String nombreProveedor,
        String numeroFactura,
        String serie,
        String correlativo,
        LocalDate fechaEmision,
        String observaciones
) {

    public static FacturaCompraDto from(FacturaCompra factura) {
        return new FacturaCompraDto(
                factura.getId(),
                factura.getProveedor().getCodigo(),
                factura.getProveedor().getRuc(),
                factura.getProveedor().getNombre(),
                factura.getNumeroFactura(),
                factura.getSerie(),
                factura.getCorrelativo(),
                factura.getFechaEmision(),
                factura.getObservaciones()
        );
    }
}
