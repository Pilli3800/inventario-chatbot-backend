package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InventarioSedeCreateRequest(
        @NotBlank
        String codigoItem,

        @NotBlank
        String sedeCodigo
) { }