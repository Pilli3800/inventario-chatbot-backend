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
        String codigoCuadrilla,
        String servicio,
        LocalDateTime fechaMovimiento,
        String observaciones
) {

    public static MovimientoInventarioDto from(MovimientoInventario entity) {
        return new MovimientoInventarioDto(
                entity.getId(),
                entity.getTipoMovimiento().name(),

                // Item
                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getItem().getCodigoItem()
                        : entity.getInventarioDestino().getItem().getCodigoItem(),

                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getItem().getNombre()
                        : entity.getInventarioDestino().getItem().getNombre(),

                entity.getCantidad(),

                // Sede origen
                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getSede().getCodigo()
                        : null,

                // Sede destino
                entity.getInventarioDestino() != null
                        ? entity.getInventarioDestino().getSede().getCodigo()
                        : null,

                // Usuario
                entity.getUsuario().getIdentUsuario(),

                // Cuadrilla
                entity.getCuadrilla() != null
                        ? entity.getCuadrilla().getCodigoCuadrilla()
                        : null,

                // Servicio (viene de cuadrilla)
                entity.getCuadrilla() != null
                        ? entity.getCuadrilla().getServicio().getNombre()
                        : null,

                // Fecha
                entity.getFechaMovimiento(),

                // Observaciones
                entity.getObservaciones()
        );
    }
}