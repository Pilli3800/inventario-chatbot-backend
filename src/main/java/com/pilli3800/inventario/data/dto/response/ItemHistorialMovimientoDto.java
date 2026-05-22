package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.dto.response.general.CodigoNombreDto;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.item.Item;

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
        String codigoProveedor,
        String nombreProveedor,
        String numeroFactura,
        Long solicitudId,
        LocalDateTime fechaMovimiento,
        String observaciones
) {

    public static ItemHistorialMovimientoDto from(MovimientoInventario entity) {
        Item item = null;
        if (entity.getInventarioOrigen() != null) {
            item = entity.getInventarioOrigen().getItem();
        } else if (entity.getInventarioDestino() != null) {
            item = entity.getInventarioDestino().getItem();
        } else if (entity.getInventarioServicioOrigen() != null) {
            item = entity.getInventarioServicioOrigen().getItem();
        } else if (entity.getInventarioServicioDestino() != null) {
            item = entity.getInventarioServicioDestino().getItem();
        }

        Servicio servicio = null;
        if (entity.getInventarioServicioOrigen() != null) {
            servicio = entity.getInventarioServicioOrigen().getServicio();
        } else if (entity.getInventarioServicioDestino() != null) {
            servicio = entity.getInventarioServicioDestino().getServicio();
        } else if (entity.getCuadrilla() != null) {
            servicio = entity.getCuadrilla().getServicio();
        }

        String codigoProveedor = entity.getProveedor() != null
                ? entity.getProveedor().getCodigo()
                : entity.getFacturaCompra() != null
                ? entity.getFacturaCompra().getProveedor().getCodigo()
                : null;
        String nombreProveedor = entity.getProveedor() != null
                ? entity.getProveedor().getNombre()
                : entity.getFacturaCompra() != null
                ? entity.getFacturaCompra().getProveedor().getNombre()
                : null;
        String numeroFactura = entity.getFacturaCompra() != null
                ? entity.getFacturaCompra().getNumeroFactura()
                : entity.getNumeroFactura();

        return new ItemHistorialMovimientoDto(
                entity.getId(),
                entity.getTipoMovimiento().name(),

                item.getCodigoItem(),

                item.getNombre(),

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

                servicio != null
                        ? new CodigoNombreDto(servicio.getCodigo(), servicio.getNombre())
                        : null,

                codigoProveedor,
                nombreProveedor,
                numeroFactura,
                entity.getSolicitud() != null ? entity.getSolicitud().getId() : null,

                entity.getFechaMovimiento(),

                entity.getObservaciones()
        );
    }
}
