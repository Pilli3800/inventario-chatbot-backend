package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;

import java.time.LocalDateTime;

public record SolicitudItemsPendienteCierreDto(
        Long id,
        String codigoCuadrilla,
        String servicioOrigenCodigo,
        EstadoSolicitudItems estado,
        LocalDateTime fechaEntrega
) {
}
