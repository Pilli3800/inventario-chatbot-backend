package com.pilli3800.inventario.data.dto.request.movimientos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record MovimientoCompraCreateRequest(

        @NotBlank
        String codigoItem,

        @NotBlank
        String sedeDestinoCodigo,

        @NotBlank
        String codigoProveedor,

        String numeroFactura,

        String serieFactura,

        String correlativoFactura,

        LocalDate fechaEmisionFactura,

        @NotNull
        @Positive
        Long cantidad,

        String observaciones

) { }
