package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.Cuadrilla;

public record CuadrillaDto(
        Long id,
        String codigoCuadrilla,
        Long jefeCuadrillaId,
        String jefeCuadrillaUsuario,
        boolean enabled
) {

    public static CuadrillaDto from(Cuadrilla cuadrilla) {
        return new CuadrillaDto(
                cuadrilla.getId(),
                cuadrilla.getCodigoCuadrilla(),
                cuadrilla.getJefeCuadrilla().getId(),
                cuadrilla.getJefeCuadrilla().getIdentUsuario(),
                cuadrilla.isEnabled()
        );
    }
}