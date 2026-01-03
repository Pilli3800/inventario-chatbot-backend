package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SedeUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 255)
        String descripcion
) { }
