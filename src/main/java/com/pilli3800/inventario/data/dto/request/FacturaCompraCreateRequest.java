package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record FacturaCompraCreateRequest(
        @NotBlank
        String codigoProveedor,
        String numeroFactura,
        String serie,
        String correlativo,
        LocalDate fechaEmision,
        String observaciones
) { }
