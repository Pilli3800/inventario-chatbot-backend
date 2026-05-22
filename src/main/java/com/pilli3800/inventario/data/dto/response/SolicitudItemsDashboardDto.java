package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;

import java.util.List;
import java.util.Map;

public record SolicitudItemsDashboardDto(
        Long total,
        Long abiertas,
        Long finales,
        Long pendientes,
        Long aprobadas,
        Long rechazadas,
        Long entregadas,
        Long devueltas,
        Long cerradasSinDevolucion,
        Map<EstadoSolicitudItems, Long> porEstado,
        List<SolicitudItemsDashboardPorServicioDto> porServicio,
        List<SolicitudItemsPendienteCierreDto> pendientesCierre
) {
}
