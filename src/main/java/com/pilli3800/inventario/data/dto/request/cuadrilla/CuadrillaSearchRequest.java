package com.pilli3800.inventario.data.dto.request.cuadrilla;

public record CuadrillaSearchRequest(
        String codigoCuadrilla,
        String identUsuarioJefe,
        Boolean enabled
) {}
