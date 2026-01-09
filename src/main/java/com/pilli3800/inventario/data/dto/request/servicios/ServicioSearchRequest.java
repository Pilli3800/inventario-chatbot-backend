package com.pilli3800.inventario.data.dto.request.servicios;

public record ServicioSearchRequest(
        String codigo,
        String nombre,
        Boolean enabled
) {}