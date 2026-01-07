package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.MovimientoInventario;

import java.time.LocalDateTime;

public record MovimientoInventarioDto(
        Long id,
        String tipoMovimiento,
        String codigoItem,
        String nombreItem,
        Long cantidad,
        String sedeOrigen,
        String sedeDestino,
        String usuario,
        LocalDateTime fechaMovimiento,
        String observaciones
) {

    public static MovimientoInventarioDto from(MovimientoInventario entity) {
        return new MovimientoInventarioDto(
                entity.getId(),
                entity.getTipoMovimiento().name(),

                // Item (origen o destino según tipo)
                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getItem().getCodigoItem()
                        : entity.getInventarioDestino().getItem().getCodigoItem(),

                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getItem().getNombre()
                        : entity.getInventarioDestino().getItem().getNombre(),

                entity.getCantidad(),

                // Sede origen (puede ser null)
                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getSede().getCodigo()
                        : null,

                // Sede destino (puede ser null)
                entity.getInventarioDestino() != null
                        ? entity.getInventarioDestino().getSede().getCodigo()
                        : null,

                entity.getUsuario().getIdentUsuario(),
                entity.getFechaMovimiento(),
                entity.getObservaciones()
        );
    }
}