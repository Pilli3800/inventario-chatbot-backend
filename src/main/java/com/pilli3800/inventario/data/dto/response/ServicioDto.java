package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.Servicio;

public record ServicioDto(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        boolean enabled
) {
    public static ServicioDto from(Servicio servicio) {
        return new ServicioDto(
                servicio.getId(),
                servicio.getCodigo(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.isEnabled()
        );
    }
}