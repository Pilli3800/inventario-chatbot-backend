package com.pilli3800.inventario.data.dto.request;

public record UserSearchRequest(
        String nombres,
        String apellidos,
        String email,
        String identUsuario,
        String dni,
        Boolean enabled,
        String rol
) { }
