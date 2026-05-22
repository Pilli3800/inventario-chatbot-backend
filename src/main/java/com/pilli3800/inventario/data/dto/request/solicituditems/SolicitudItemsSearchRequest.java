package com.pilli3800.inventario.data.dto.request.solicituditems;

import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;

import java.time.LocalDate;

public record SolicitudItemsSearchRequest(
        String codigoCuadrilla,
        String identUsuario,
        String servicioOrigenCodigo,
        EstadoSolicitudItems estado,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Long solicitanteId
) {
}

