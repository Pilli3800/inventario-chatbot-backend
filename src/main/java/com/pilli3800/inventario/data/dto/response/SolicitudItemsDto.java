package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import com.pilli3800.inventario.data.models.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitudItemsDto(
        Long id,
        String codigoCuadrilla,
        String codigoJefeCuadrilla,
        String nombreJefeCuadrilla,
        String servicioOrigenCodigo,
        EstadoSolicitudItems estado,
        String observaciones,
        String observacionesAprobacion,
        String observacionesEntrega,
        String observacionesDevolucion,
        String observacionesCierre,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaAprobacion,
        LocalDateTime fechaEntrega,
        LocalDateTime fechaDevolucion,
        LocalDateTime fechaCierre,
        String codigoSolicitante,
        String nombreSolicitante,
        String codigoUsuarioAprobacion,
        String nombreUsuarioAprobacion,
        String codigoUsuarioRechazo,
        String nombreUsuarioRechazo,
        String codigoUsuarioEntrega,
        String nombreUsuarioEntrega,
        String codigoUsuarioDevolucion,
        String nombreUsuarioDevolucion,
        String codigoUsuarioCierre,
        String nombreUsuarioCierre,
        List<SolicitudItemsDetalleDto> detalles
) {
    public static SolicitudItemsDto from(SolicitudItems solicitud) {
        List<SolicitudItemsDetalleDto> detalles = solicitud.getDetalles()
                .stream()
                .map(SolicitudItemsDetalleDto::from)
                .toList();

        return new SolicitudItemsDto(
                solicitud.getId(),
                solicitud.getCuadrilla().getCodigoCuadrilla(),
                solicitud.getCuadrilla().getJefeCuadrilla().getIdentUsuario(),
                nombreCompleto(solicitud.getCuadrilla().getJefeCuadrilla()),
                codigoServicio(solicitud),
                solicitud.getEstado(),
                solicitud.getObservaciones(),
                solicitud.getObservacionesAprobacion(),
                solicitud.getObservacionesEntrega(),
                solicitud.getObservacionesDevolucion(),
                solicitud.getObservacionesCierre(),
                solicitud.getFcCreacion(),
                solicitud.getFechaAprobacion(),
                solicitud.getFechaEntrega(),
                solicitud.getFechaDevolucion(),
                solicitud.getFechaCierre(),
                solicitud.getSolicitante().getIdentUsuario(),
                nombreCompleto(solicitud.getSolicitante()),
                codigoUsuario(solicitud.getUsuarioAprobacion()),
                nombreCompleto(solicitud.getUsuarioAprobacion()),
                codigoUsuario(solicitud.getUsuarioRechazo()),
                nombreCompleto(solicitud.getUsuarioRechazo()),
                codigoUsuario(solicitud.getUsuarioEntrega()),
                nombreCompleto(solicitud.getUsuarioEntrega()),
                codigoUsuario(solicitud.getUsuarioDevolucion()),
                nombreCompleto(solicitud.getUsuarioDevolucion()),
                codigoUsuario(solicitud.getUsuarioCierre()),
                nombreCompleto(solicitud.getUsuarioCierre()),
                detalles
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

    private static String codigoUsuario(User usuario) {
        return usuario != null ? usuario.getIdentUsuario() : null;
    }

    private static String codigoServicio(SolicitudItems solicitud) {
        return solicitud.getServicioOrigen() != null ? solicitud.getServicioOrigen().getCodigo() : null;
    }
}

