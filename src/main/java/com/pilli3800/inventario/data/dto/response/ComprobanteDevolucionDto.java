package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucion;
import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucionDetalle;
import com.pilli3800.inventario.data.models.user.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ComprobanteDevolucionDto(
        Long id,
        Long solicitudId,
        String numeroComprobante,
        LocalDateTime fechaGeneracion,
        String codigoCuadrilla,
        String nombreJefeCuadrilla,
        String nombreSolicitante,
        LocalDateTime fechaEntregaSolicitud,
        LocalDateTime fechaDevolucionSolicitud,
        String observacionesSolicitud,
        String observacionesEntrega,
        String observacionesDevolucion,
        List<ComprobanteDevolucionDetalleDto> detalles,
        List<ComprobanteDevolucionFirmaDto> firmas
) {
    public static ComprobanteDevolucionDto from(ComprobanteDevolucion comprobante) {
        List<ComprobanteDevolucionDetalleDto> detalles = comprobante.getDetalles()
                .stream()
                .sorted(Comparator.comparing(ComprobanteDevolucionDetalle::getNumeroItem))
                .map(ComprobanteDevolucionDetalleDto::from)
                .toList();

        List<ComprobanteDevolucionFirmaDto> firmas = comprobante.getFirmas()
                .stream()
                .sorted(Comparator.comparing(f -> f.getRolFirma().name()))
                .map(ComprobanteDevolucionFirmaDto::from)
                .toList();

        return new ComprobanteDevolucionDto(
                comprobante.getId(),
                comprobante.getSolicitud().getId(),
                comprobante.getNumeroComprobante(),
                comprobante.getFechaGeneracion(),
                comprobante.getSolicitud().getCuadrilla().getCodigoCuadrilla(),
                nombreCompleto(comprobante.getSolicitud().getCuadrilla().getJefeCuadrilla()),
                nombreCompleto(comprobante.getSolicitud().getSolicitante()),
                comprobante.getSolicitud().getFechaEntrega(),
                comprobante.getSolicitud().getFechaDevolucion(),
                comprobante.getSolicitud().getObservaciones(),
                comprobante.getSolicitud().getObservacionesEntrega(),
                comprobante.getSolicitud().getObservacionesDevolucion(),
                detalles,
                firmas
        );
    }

    private static String nombreCompleto(User usuario) {
        if (usuario == null) {
            return null;
        }
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? null : full;
    }
}
