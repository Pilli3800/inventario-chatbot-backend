package com.pilli3800.inventario.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pilli3800.inventario.data.dto.response.ValeSalidaDto;
import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.data.models.enums.RolValeSalidaFirma;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItemsDetalle;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.data.models.valesalida.ValeSalida;
import com.pilli3800.inventario.data.models.valesalida.ValeSalidaDetalle;
import com.pilli3800.inventario.data.models.valesalida.ValeSalidaFirma;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.SolicitudItemsRepository;
import com.pilli3800.inventario.repository.ValeSalidaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.pilli3800.inventario.util.DateFormat.formatearFechaHora;
import static com.pilli3800.inventario.util.TextFormat.reemplazarNuloPorVacio;

@Service
@RequiredArgsConstructor
public class ValeSalidaService {

    private final ValeSalidaRepository valeSalidaRepository;
    private final SolicitudItemsRepository solicitudItemsRepository;

    @Transactional
    public ValeSalidaDto generarValeParaSolicitud(Long solicitudId) {
        SolicitudItems solicitud = obtenerSolicitudPorId(solicitudId);
        ValeSalida vale = generarValeSiNoExisteInterno(solicitud);
        return ValeSalidaDto.from(obtenerValePorId(vale.getId()));
    }

    @Transactional
    public void generarValeSiNoExiste(SolicitudItems solicitud) {
        generarValeSiNoExisteInterno(solicitud);
    }

    @Transactional
    public ValeSalidaDto getVale(Long valeId) {
        return ValeSalidaDto.from(obtenerValePorId(valeId));
    }

    @Transactional
    public ValeSalidaDto getValePorSolicitud(Long solicitudId) {
        return ValeSalidaDto.from(obtenerValePorSolicitudId(solicitudId));
    }

    @Transactional
    public byte[] generarPdfVale(Long valeId) {
        ValeSalida vale = obtenerValePorId(valeId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            agregarEncabezado(document, vale);
            agregarDatosGenerales(document, vale);
            agregarTablaDetalles(document, vale);
            agregarFirmas(document, vale);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ValidationException(List.of("No se pudo generar el PDF del vale"));
        }
    }

    private ValeSalida generarValeSiNoExisteInterno(SolicitudItems solicitud) {
        if (valeSalidaRepository.existsBySolicitudId(solicitud.getId())) {
            return obtenerValePorSolicitudId(solicitud.getId());
        }

        validarSolicitudEntregada(solicitud);

        ValeSalida vale = new ValeSalida();
        vale.setSolicitud(solicitud);
        vale.setFechaGeneracion(LocalDateTime.now());
        vale.setObservaciones(solicitud.getObservacionesEntrega());
        vale.setNumeroVale("TMP-" + UUID.randomUUID().toString().substring(0, 8));

        int numero = 1;
        for (SolicitudItemsDetalle solicitudDetalle : solicitud.getDetalles()) {
            ValeSalidaDetalle detalle = new ValeSalidaDetalle();
            detalle.setValeSalida(vale);
            detalle.setItem(solicitudDetalle.getItem());
            detalle.setNumeroItem(numero++);
            detalle.setCantidadEntregada(solicitudDetalle.getCantidad());
            detalle.setCodigoItemSnapshot(solicitudDetalle.getItem().getCodigoItem());
            detalle.setNombreItemSnapshot(solicitudDetalle.getItem().getNombre());
            detalle.setDescripcionItemSnapshot(solicitudDetalle.getItem().getDescripcion());
            detalle.setOrdenTrabajo(obtenerProyectoDesdeSolicitud(solicitud));
            detalle.setEstadoObservacion(null);
            vale.getDetalles().add(detalle);
        }

        vale.getFirmas().add(crearFirma(
                vale,
                RolValeSalidaFirma.JEFE_CUADRILLA,
                solicitud.getCuadrilla().getJefeCuadrilla(),
                solicitud.getFechaAprobacion() != null
                        ? solicitud.getFechaAprobacion()
                        : solicitud.getFcCreacion()
        ));

        vale.getFirmas().add(crearFirma(
                vale,
                RolValeSalidaFirma.LOGISTICA,
                solicitud.getUsuarioEntrega(),
                solicitud.getFechaEntrega()
        ));

        ValeSalida guardado = valeSalidaRepository.save(vale);
        guardado.setNumeroVale(generarNumeroVale(guardado.getId()));

        return valeSalidaRepository.save(guardado);
    }

    private ValeSalidaFirma crearFirma(
            ValeSalida vale,
            RolValeSalidaFirma rol,
            User usuario,
            LocalDateTime fechaFirma
    ) {
        if (usuario == null) {
            throw new ValidationException(List.of(
                    "No se pudo generar firma " + rol + " para el vale"
            ));
        }

        ValeSalidaFirma firma = new ValeSalidaFirma();
        firma.setValeSalida(vale);
        firma.setRolFirma(rol);
        firma.setCodigoUsuarioSnapshot(usuario.getIdentUsuario());
        firma.setNombreUsuarioSnapshot(nombreCompleto(usuario));
        firma.setDniSnapshot(usuario.getDni());
        firma.setFechaFirma(fechaFirma != null ? fechaFirma : LocalDateTime.now());
        return firma;
    }

    private void validarSolicitudEntregada(SolicitudItems solicitud) {
        if (solicitud.getEstado() != EstadoSolicitudItems.ENTREGADO) {
            throw new ValidationException(List.of(
                    "Solo se puede generar vale para solicitudes entregadas"
            ));
        }

        if (solicitud.getUsuarioEntrega() == null || solicitud.getFechaEntrega() == null) {
            throw new ValidationException(List.of(
                    "La solicitud entregada no tiene datos completos de entrega"
            ));
        }
    }

    private SolicitudItems obtenerSolicitudPorId(Long solicitudId) {
        return solicitudItemsRepository.findById(solicitudId)
                .orElseThrow(() -> new ValidationException(List.of("Solicitud no encontrada")));
    }

    private ValeSalida obtenerValePorId(Long valeId) {
        return valeSalidaRepository.findById(valeId)
                .orElseThrow(() -> new ValidationException(List.of("Vale de salida no encontrado")));
    }

    private ValeSalida obtenerValePorSolicitudId(Long solicitudId) {
        return valeSalidaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ValidationException(List.of("Vale de salida no encontrado para la solicitud")));
    }

    private String generarNumeroVale(Long id) {
        return "VS-" + String.format("%06d", id);
    }

    private String nombreCompleto(User usuario) {
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
        return (nombres + " " + apellidos).trim();
    }

    private String obtenerProyectoDesdeSolicitud(SolicitudItems solicitud) {
        if (solicitud == null || solicitud.getCuadrilla() == null || solicitud.getCuadrilla().getServicio() == null) {
            return null;
        }
        String codigo = solicitud.getCuadrilla().getServicio().getCodigo();
        String nombre = solicitud.getCuadrilla().getServicio().getNombre();
        if (codigo == null || codigo.isBlank()) {
            return nombre;
        }
        if (nombre == null || nombre.isBlank()) {
            return codigo;
        }
        return codigo + " - " + nombre;
    }

    private void agregarEncabezado(Document document, ValeSalida vale) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titulo = new Paragraph("VALE DE SALIDA", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph codigo = new Paragraph("Formato: ELEMEC-001    Nro Vale: " + vale.getNumeroVale(), normal);
        codigo.setSpacingAfter(8f);
        document.add(codigo);
    }

    private void agregarDatosGenerales(Document document, ValeSalida vale) throws DocumentException {
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{2f, 4f, 2f, 4f});

        agregarCelda(table, "Cuadrilla", true);
        agregarCelda(table, reemplazarNuloPorVacio(vale.getSolicitud().getCuadrilla().getCodigoCuadrilla()), false);
        agregarCelda(table, "Fecha", true);
        agregarCelda(table, formatearFechaHora(vale.getFechaGeneracion()), false);

        agregarCelda(table, "Nombre", true);
        agregarCelda(table, reemplazarNuloPorVacio(nombreCompleto(vale.getSolicitud().getSolicitante())), false);
        agregarCelda(table, "Estado Solicitud", true);
        agregarCelda(table, reemplazarNuloPorVacio(vale.getSolicitud().getEstado().name()), false);

        if (vale.getSolicitud().getObservacionesEntrega() != null) {
            agregarCelda(table, "Obs. Entrega", true);
            PdfPCell obs = new PdfPCell(new Phrase(reemplazarNuloPorVacio(vale.getSolicitud().getObservacionesEntrega()), normal));
            obs.setColspan(3);
            table.addCell(obs);
        }

        document.add(table);
    }

    private void agregarTablaDetalles(Document document, ValeSalida vale) throws DocumentException {
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 8);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12f);
        table.setWidths(new float[]{1f, 2.2f, 4.2f, 1.4f, 2.4f, 2.8f});

        agregarCelda(table, "ITEM", true);
        agregarCelda(table, "CODIGO", true);
        agregarCelda(table, "DESCRIPCION", true);
        agregarCelda(table, "CANT", true);
        agregarCelda(table, "SERVICIO", true);
        agregarCelda(table, "ESTADO/OBS.", true);

        vale.getDetalles().stream()
                .sorted(Comparator.comparing(ValeSalidaDetalle::getNumeroItem))
                .forEach(detalle -> {
                    table.addCell(new Phrase(String.valueOf(detalle.getNumeroItem()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getCodigoItemSnapshot()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getNombreItemSnapshot()), small));
                    table.addCell(new Phrase(String.valueOf(detalle.getCantidadEntregada()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(obtenerProyectoDesdeSolicitud(vale.getSolicitud())), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getEstadoObservacion()), small));
                });

        document.add(table);
    }

    private void agregarFirmas(Document document, ValeSalida vale) throws DocumentException {
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f});

        ValeSalidaFirma jefe = vale.getFirmas().stream()
                .filter(f -> f.getRolFirma() == RolValeSalidaFirma.JEFE_CUADRILLA)
                .findFirst()
                .orElse(null);
        ValeSalidaFirma logistica = vale.getFirmas().stream()
                .filter(f -> f.getRolFirma() == RolValeSalidaFirma.LOGISTICA)
                .findFirst()
                .orElse(null);

        table.addCell(celdaFirma("JEFE CUADRILLA", jefe, normal));
        table.addCell(celdaFirma("LOGISTICA", logistica, normal));

        document.add(table);
    }

    private PdfPCell celdaFirma(String titulo, ValeSalidaFirma firma, Font font) {
        StringBuilder sb = new StringBuilder();
        sb.append(titulo).append("\n\n");
        sb.append("Nombre: ").append(firma != null ? reemplazarNuloPorVacio(firma.getNombreUsuarioSnapshot()) : "").append("\n");
        sb.append("DNI: ").append(firma != null ? reemplazarNuloPorVacio(firma.getDniSnapshot()) : "").append("\n");
        sb.append("Fecha: ").append(firma != null ? formatearFechaHora(firma.getFechaFirma()) : "");

        PdfPCell cell = new PdfPCell(new Phrase(sb.toString(), font));
        cell.setFixedHeight(90f);
        return cell;
    }

    private void agregarCelda(PdfPTable table, String valor, boolean encabezado) {
        Font font = encabezado
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)
                : FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(reemplazarNuloPorVacio(valor), font));
        table.addCell(cell);
    }
}
