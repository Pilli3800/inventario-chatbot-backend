package com.pilli3800.inventario.data.dto.response.ia;

public record IaChatDashboardResumenDto(
        Long totalConsultas,
        Long consultasExitosas,
        Long consultasFallidas,
        Long totalSesiones,
        Long usuariosQueUsaronIA
) {
}
