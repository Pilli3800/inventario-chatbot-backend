package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InventarioServicioCreateRequest(
        @NotBlank
        String codigoItem,

        @NotBlank
        String servicioCodigo
) { }
