package com.pilli3800.inventario.data.dto.request.cuadrilla;

public record CuadrillaUpdateRequest(
        String codigoUsuario,
        String codigoServicio,
        Boolean enabled
) {}