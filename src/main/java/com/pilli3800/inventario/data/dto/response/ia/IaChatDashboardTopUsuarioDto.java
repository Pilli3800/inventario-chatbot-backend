package com.pilli3800.inventario.data.dto.response.ia;

public record IaChatDashboardTopUsuarioDto(
        String usuario,
        Long totalConsultas,
        Long totalSesiones
) {
}
