package com.pilli3800.inventario.data.dto.request.conteofisico;

import jakarta.validation.constraints.NotBlank;

public record ConteoFisicoDetalleRequest(
        @NotBlank
        String codigoItem,

        Long stockSistema,

        Long cantidadFisica,

        String observacion
) {
}
