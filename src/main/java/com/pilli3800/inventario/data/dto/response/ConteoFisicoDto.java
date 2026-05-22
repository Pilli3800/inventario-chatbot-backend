package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.conteofisico.ConteoFisico;
import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import com.pilli3800.inventario.data.models.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record ConteoFisicoDto(
        Long id,
        TipoInventarioConteo tipoInventario,
        String codigoUbicacion,
        String nombreUbicacion,
        LocalDateTime fechaConteo,
        String codigoUsuario,
        String nombreUsuario,
        String observaciones,
        int totalItems,
        long itemsConDiscrepancia,
        List<ConteoFisicoDetalleDto> detalles
) {
    public static ConteoFisicoDto from(ConteoFisico conteo) {
        List<ConteoFisicoDetalleDto> detalles = conteo.getDetalles()
                .stream()
                .map(ConteoFisicoDetalleDto::from)
                .toList();

        return new ConteoFisicoDto(
                conteo.getId(),
                conteo.getTipoInventario(),
                codigoUbicacion(conteo),
                nombreUbicacion(conteo),
                conteo.getFechaConteo(),
                codigoUsuario(conteo.getUsuario()),
                nombreCompleto(conteo.getUsuario()),
                conteo.getObservaciones(),
                conteo.getDetalles().size(),
                conteo.getDetalles()
                        .stream()
                        .filter(detalle -> detalle.getDiferencia() != 0)
                        .count(),
                detalles
        );
    }

    public static ConteoFisicoDto resumen(ConteoFisico conteo) {
        return new ConteoFisicoDto(
                conteo.getId(),
                conteo.getTipoInventario(),
                codigoUbicacion(conteo),
                nombreUbicacion(conteo),
                conteo.getFechaConteo(),
                codigoUsuario(conteo.getUsuario()),
                nombreCompleto(conteo.getUsuario()),
                conteo.getObservaciones(),
                conteo.getDetalles().size(),
                conteo.getDetalles()
                        .stream()
                        .filter(detalle -> detalle.getDiferencia() != 0)
                        .count(),
                null
        );
    }

    private static String codigoUbicacion(ConteoFisico conteo) {
        return conteo.getTipoInventario() == TipoInventarioConteo.SEDE
                ? conteo.getSede().getCodigo()
                : conteo.getServicio().getCodigo();
    }

    private static String nombreUbicacion(ConteoFisico conteo) {
        return conteo.getTipoInventario() == TipoInventarioConteo.SEDE
                ? conteo.getSede().getNombre()
                : conteo.getServicio().getNombre();
    }

    private static String codigoUsuario(User usuario) {
        return usuario != null ? usuario.getIdentUsuario() : null;
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
