package com.pilli3800.inventario.data.dto.request.servicios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServicioCreateRequest(
        @NotBlank
        @Size(max = 8)
        String codigoServicio,

        @NotBlank
        String nombreServicio,

        String descripcionServicio
) {}