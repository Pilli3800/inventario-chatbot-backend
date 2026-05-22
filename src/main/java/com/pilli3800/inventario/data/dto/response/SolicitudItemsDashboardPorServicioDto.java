package com.pilli3800.inventario.data.dto.response;

public record SolicitudItemsDashboardPorServicioDto(
        String servicioOrigenCodigo,
        Long total,
        Long pendientes,
        Long aprobadas,
        Long entregadas,
        Long devueltas,
        Long cerradasSinDevolucion,
        Long rechazadas
) {
}
