package com.pilli3800.inventario.data.dto.request;

public record ProveedorSearchRequest(
        String codigo,
        String ruc,
        String nombre,
        Boolean enabled
) { }
