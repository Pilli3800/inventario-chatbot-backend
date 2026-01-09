package com.pilli3800.inventario.data.dto.request.cuadrilla;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CuadrillaCreateRequest(
        @NotBlank
        @Size(max = 8)
        @Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "El código solo puede contener letras y números"
        )
        String codigoCuadrilla,

        @NotBlank
        String codigoUsuario,

        @NotBlank
        @Size(max = 8)
        @Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "El código solo puede contener letras y números"
        )
        String codigoServicio
) {}