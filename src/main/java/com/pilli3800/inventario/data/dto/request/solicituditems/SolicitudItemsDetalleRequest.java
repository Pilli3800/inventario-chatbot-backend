package com.pilli3800.inventario.data.dto.request.solicituditems;

import jakarta.validation.constraints.NotBlank;

public record SolicitudItemsDetalleRequest(
        @NotBlank
        String codigoItem,

        Long cantidad
) {
}

