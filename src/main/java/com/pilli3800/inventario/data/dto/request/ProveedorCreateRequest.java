package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorCreateRequest(
        @NotBlank
        String ruc,
        @NotBlank
        String nombre,
        @Email
        @Size(max = 150)
        String email,
        @Size(max = 9)
        String telefono,
        @Size(max = 255)
        String observaciones
) { }
