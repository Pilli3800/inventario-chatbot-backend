package com.pilli3800.inventario.data.dto.response.ia;

import java.util.List;

public record IaChatDashboardDto(
        Long totalConsultas,
        Long consultasExitosas,
        Long consultasFallidas,
        Long totalSesiones,
        Long usuariosQueUsaronIA,
        Long totalUsuariosActivos,
        Double porcentajeUsuariosQueUsaronIA,
        Double promedioConsultasPorSesion,
        Long consultasHoy,
        Long consultasUltimos7Dias,
        Long consultasUltimos30Dias,
        List<IaChatDashboardFechaDto> porFecha,
        List<IaChatDashboardTopUsuarioDto> topUsuarios
) {
}
