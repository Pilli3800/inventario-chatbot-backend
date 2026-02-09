package com.pilli3800.inventario.data.dto.request.solicituditems;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitudItemsCreateRequest(
        @NotBlank
        String codigoCuadrilla,

        @NotBlank
        String sedeOrigenCodigo,

        String observaciones,

        @NotNull
        @Size(min = 1)
        List<SolicitudItemsDetalleRequest> detalles
) {
}

