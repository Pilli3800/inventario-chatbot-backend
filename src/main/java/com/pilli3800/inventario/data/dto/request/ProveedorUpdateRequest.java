package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProveedorUpdateRequest(
        String ruc,
        String nombre,
        @Email
        @Size(max = 150)
        String email,
        @Size(max = 9)
        String telefono,
        @Size(max = 255)
        String observaciones,
        Boolean enabled
) { }
