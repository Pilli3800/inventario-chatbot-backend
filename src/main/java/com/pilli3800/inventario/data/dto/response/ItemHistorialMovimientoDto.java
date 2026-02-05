package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.dto.response.general.CodigoNombreDto;
import com.pilli3800.inventario.data.models.MovimientoInventario;

import java.time.LocalDateTime;

public record ItemHistorialMovimientoDto(
        Long id,
        String tipoMovimiento,
        String codigoItem,
        String nombreItem,
        Long cantidad,
        String sedeOrigen,
        String sedeDestino,
        String usuario,
        String codigoCuadrilla,
        CodigoNombreDto servicio,
        LocalDateTime fechaMovimiento,
        String observaciones
) {

    public static ItemHistorialMovimientoDto from(MovimientoInventario entity) {
        return new ItemHistorialMovimientoDto(
                entity.getId(),
                entity.getTipoMovimiento().name(),

                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getItem().getCodigoItem()
                        : entity.getInventarioDestino().getItem().getCodigoItem(),

                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getItem().getNombre()
                        : entity.getInventarioDestino().getItem().getNombre(),

                entity.getCantidad(),

                entity.getInventarioOrigen() != null
                        ? entity.getInventarioOrigen().getSede().getCodigo()
                        : null,

                entity.getInventarioDestino() != null
                        ? entity.getInventarioDestino().getSede().getCodigo()
                        : null,

                entity.getUsuario().getIdentUsuario(),

                entity.getCuadrilla() != null
                        ? entity.getCuadrilla().getCodigoCuadrilla()
                        : null,

                entity.getCuadrilla() != null
                        ? new CodigoNombreDto(
                        entity.getCuadrilla().getServicio().getCodigo(),
                        entity.getCuadrilla().getServicio().getNombre()
                )
                        : null,

                entity.getFechaMovimiento(),

                entity.getObservaciones()
        );
    }
}
