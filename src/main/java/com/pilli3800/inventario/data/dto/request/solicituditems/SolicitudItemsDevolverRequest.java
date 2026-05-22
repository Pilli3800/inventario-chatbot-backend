package com.pilli3800.inventario.data.dto.request.solicituditems;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitudItemsDevolverRequest(
        String observaciones,

        @NotNull
        @Size(min = 1)
        List<@Valid SolicitudItemsDetalleRequest> detalles
) {
}
