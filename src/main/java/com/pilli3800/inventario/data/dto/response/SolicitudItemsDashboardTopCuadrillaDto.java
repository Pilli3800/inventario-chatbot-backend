package com.pilli3800.inventario.data.dto.response;

public record SolicitudItemsDashboardTopCuadrillaDto(
        String codigoCuadrilla,
        String servicioCodigo,
        String servicioNombre,
        String jefeCuadrillaIdentUsuario,
        String jefeCuadrillaNombreCompleto,
        Long totalSolicitudes
) {
}
