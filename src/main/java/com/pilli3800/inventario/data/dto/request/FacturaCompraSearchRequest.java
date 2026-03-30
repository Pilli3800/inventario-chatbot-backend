package com.pilli3800.inventario.data.dto.request;

public record FacturaCompraSearchRequest(
        String codigoProveedor,
        String numeroFactura
) { }
