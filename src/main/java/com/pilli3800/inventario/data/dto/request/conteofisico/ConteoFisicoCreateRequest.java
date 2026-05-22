package com.pilli3800.inventario.data.dto.request.conteofisico;

import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConteoFisicoCreateRequest(
        @NotNull
        TipoInventarioConteo tipoInventario,

        @NotBlank
        String codigoUbicacion,

        String observaciones,

        @NotNull
        @Size(min = 1)
        List<ConteoFisicoDetalleRequest> detalles
) {
}
