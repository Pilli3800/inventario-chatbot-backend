package com.pilli3800.inventario.data.dto.request.servicios;

public record ServicioUpdateRequest(
        String nombreServicio,
        String descripcionServicio,
        Boolean enabled
) {}