package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.Sede;

public record SedeDto(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        boolean enabled
) {

    public static SedeDto from(Sede sede) {
        return new SedeDto(
                sede.getId(),
                sede.getCodigo(),
                sede.getNombre(),
                sede.getDescripcion(),
                sede.isEnabled()
        );
    }
}