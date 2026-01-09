package com.pilli3800.inventario.data.dto.request.cuadrilla;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CuadrillaCreateRequest(
        @NotBlank
        @Size(max = 8)
        String codigoCuadrilla,

        @NotBlank
        String codigoUsuario,

        @NotBlank
        @Size(max = 8)
        String codigoServicio
) {}