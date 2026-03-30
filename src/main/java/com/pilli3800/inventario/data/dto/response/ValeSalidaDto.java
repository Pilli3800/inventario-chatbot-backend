package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.valesalida.ValeSalidaDetalle;
import com.pilli3800.inventario.data.models.valesalida.ValeSalida;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ValeSalidaDto(
        Long id,
        Long solicitudId,
        String numeroVale,
        LocalDateTime fechaGeneracion,
        String codigoCuadrilla,
        String nombreJefeCuadrilla,
        String nombreSolicitante,
        LocalDateTime fechaAprobacionSolicitud,
        LocalDateTime fechaEntregaSolicitud,
        String observacionesSolicitud,
        String observacionesAprobacion,
        String observacionesEntrega,
        List<ValeSalidaDetalleDto> detalles,
        List<ValeSalidaFirmaDto> firmas
) {
    public static ValeSalidaDto from(ValeSalida vale) {
        List<ValeSalidaDetalleDto> detalles = vale.getDetalles()
                .stream()
                .sorted(Comparator.comparing(ValeSalidaDetalle::getNumeroItem))
                .map(ValeSalidaDetalleDto::from)
                .toList();

        List<ValeSalidaFirmaDto> firmas = vale.getFirmas()
                .stream()
                .sorted(Comparator.comparing(f -> f.getRolFirma().name()))
                .map(ValeSalidaFirmaDto::from)
                .toList();

        return new ValeSalidaDto(
                vale.getId(),
                vale.getSolicitud().getId(),
                vale.getNumeroVale(),
                vale.getFechaGeneracion(),
                vale.getSolicitud().getCuadrilla().getCodigoCuadrilla(),
                nombreCompleto(vale.getSolicitud().getCuadrilla().getJefeCuadrilla()),
                nombreCompleto(vale.getSolicitud().getSolicitante()),
                vale.getSolicitud().getFechaAprobacion(),
                vale.getSolicitud().getFechaEntrega(),
                vale.getSolicitud().getObservaciones(),
                vale.getSolicitud().getObservacionesAprobacion(),
                vale.getSolicitud().getObservacionesEntrega(),
                detalles,
                firmas
        );
    }

    private static String nombreCompleto(com.pilli3800.inventario.data.models.user.User usuario) {
        if (usuario == null) {
            return null;
        }
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
        String full = (nombres + " " + apellidos).trim();
        return full.isEmpty() ? null : full;
    }
}
