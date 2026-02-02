package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.Cuadrilla;

public record CuadrillaDto(
        Long id,
        String codigoCuadrilla,
        String codigoServicio,
        Long jefeCuadrillaId,
        String jefeCuadrillaUsuario,
        String jefeCuadrillaNombresyApellidos,
        boolean enabled
) {

    public static CuadrillaDto from(Cuadrilla cuadrilla) {
        return new CuadrillaDto(
                cuadrilla.getId(),
                cuadrilla.getCodigoCuadrilla(),
                cuadrilla.getServicio().getCodigo(),
                cuadrilla.getJefeCuadrilla().getId(),
                cuadrilla.getJefeCuadrilla().getIdentUsuario(),
                cuadrilla.getJefeCuadrilla().getNombres()+" "+cuadrilla.getJefeCuadrilla().getApellidos(),
                cuadrilla.isEnabled()
        );
    }
}