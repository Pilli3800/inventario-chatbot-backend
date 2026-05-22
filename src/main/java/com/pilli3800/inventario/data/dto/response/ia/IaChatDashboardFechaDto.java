package com.pilli3800.inventario.data.dto.response.ia;

import java.time.LocalDate;

public record IaChatDashboardFechaDto(
        LocalDate fecha,
        Long consultas,
        Long sesiones,
        Long usuarios
) {
}
