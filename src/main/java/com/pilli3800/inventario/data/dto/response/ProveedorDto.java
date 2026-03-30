package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.Proveedor;

public record ProveedorDto(
        Long id,
        String codigo,
        String ruc,
        String nombre,
        String email,
        String telefono,
        String observaciones,
        boolean enabled
) {

    public static ProveedorDto from(Proveedor proveedor) {
        return new ProveedorDto(
                proveedor.getId(),
                proveedor.getCodigo(),
                proveedor.getRuc(),
                proveedor.getNombre(),
                proveedor.getEmail(),
                proveedor.getTelefono(),
                proveedor.getObservaciones(),
                proveedor.isEnabled()
        );
    }
}
