package com.pilli3800.inventario.data.dto.request.conteofisico;

import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;

import java.time.LocalDate;

public record ConteoFisicoSearchRequest(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String usuario,
        TipoInventarioConteo tipoInventario,
        String codigoUbicacion,
        Long usuarioId
) {
}
