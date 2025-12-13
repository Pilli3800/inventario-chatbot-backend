package com.pilli3800.inventario.data.dto.request;

import java.util.Set;

public record UserUpdateRequest(
        String identUsuario,
        String dni,
        String email,
        String nombres,
        String apellidos,
        boolean enabled,
        Set<String> roles
) {}

