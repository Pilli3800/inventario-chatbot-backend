package com.pilli3800.inventario.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pilli3800.inventario.data.dto.response.ComprobanteDevolucionDto;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.data.models.enums.RolComprobanteDevolucionFirma;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucion;
import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucionDetalle;
import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucionFirma;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ComprobanteDevolucionRepository;
import com.pilli3800.inventario.repository.MovimientoInventarioRepository;
import com.pilli3800.inventario.repository.SolicitudItemsRepository;
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
public class ComprobanteDevolucionService {

    private final ComprobanteDevolucionRepository comprobanteDevolucionRepository;
    private final SolicitudItemsRepository solicitudItemsRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Transactional
    public ComprobanteDevolucionDto generarComprobanteParaSolicitud(Long solicitudId) {
        SolicitudItems solicitud = obtenerSolicitudPorId(solicitudId);
        ComprobanteDevolucion comprobante = generarComprobanteSiNoExisteInterno(solicitud);
        return ComprobanteDevolucionDto.from(obtenerComprobantePorId(comprobante.getId()));
    }

    @Transactional
    public void generarComprobanteSiNoExiste(SolicitudItems solicitud) {
        generarComprobanteSiNoExisteInterno(solicitud);
    }

    @Transactional
    public ComprobanteDevolucionDto getComprobante(Long comprobanteId) {
        return ComprobanteDevolucionDto.from(obtenerComprobantePorId(comprobanteId));
    }

    @Transactional
    public ComprobanteDevolucionDto getComprobantePorSolicitud(Long solicitudId) {
        return ComprobanteDevolucionDto.from(obtenerComprobantePorSolicitudId(solicitudId));
    }

    @Transactional
    public byte[] generarPdfComprobante(Long comprobanteId) {
        ComprobanteDevolucion comprobante = obtenerComprobantePorId(comprobanteId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            agregarEncabezado(document, comprobante);
            agregarDatosGenerales(document, comprobante);
            agregarTablaDetalles(document, comprobante);
            agregarFirmas(document, comprobante);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ValidationException(List.of("No se pudo generar el PDF del comprobante de devolucion"));
        }
    }

    private ComprobanteDevolucion generarComprobanteSiNoExisteInterno(SolicitudItems solicitud) {
        if (comprobanteDevolucionRepository.existsBySolicitudId(solicitud.getId())) {
            return obtenerComprobantePorSolicitudId(solicitud.getId());
        }

        validarSolicitudDevuelta(solicitud);

        List<MovimientoInventario> devoluciones = movimientoInventarioRepository
                .findBySolicitudIdAndTipoMovimientoOrderByIdAsc(
                        solicitud.getId(),
                        TipoMovimiento.DEVOLUCION
                );

        if (devoluciones.isEmpty()) {
            throw new ValidationException(List.of(
                    "No existen movimientos de devolucion para generar el comprobante"
            ));
        }

        ComprobanteDevolucion comprobante = new ComprobanteDevolucion();
        comprobante.setSolicitud(solicitud);
        comprobante.setFechaGeneracion(LocalDateTime.now());
        comprobante.setObservaciones(solicitud.getObservacionesDevolucion());
        comprobante.setNumeroComprobante("TMP-" + UUID.randomUUID().toString().substring(0, 8));

        int numero = 1;
        for (MovimientoInventario movimiento : devoluciones) {
            Item item = obtenerItemDesdeMovimiento(movimiento);

            ComprobanteDevolucionDetalle detalle = new ComprobanteDevolucionDetalle();
            detalle.setComprobanteDevolucion(comprobante);
            detalle.setItem(item);
            detalle.setNumeroItem(numero++);
            detalle.setCantidadDevuelta(movimiento.getCantidad());
            detalle.setCodigoItemSnapshot(item.getCodigoItem());
            detalle.setNombreItemSnapshot(item.getNombre());
            detalle.setDescripcionItemSnapshot(item.getDescripcion());
            detalle.setEstadoObservacion(movimiento.getObservaciones());
            comprobante.getDetalles().add(detalle);
        }

        comprobante.getFirmas().add(crearFirma(
                comprobante,
                RolComprobanteDevolucionFirma.JEFE_CUADRILLA,
                solicitud.getCuadrilla().getJefeCuadrilla(),
                solicitud.getFechaDevolucion()
        ));

        comprobante.getFirmas().add(crearFirma(
                comprobante,
                RolComprobanteDevolucionFirma.LOGISTICA,
                solicitud.getUsuarioDevolucion(),
                solicitud.getFechaDevolucion()
        ));

        ComprobanteDevolucion guardado = comprobanteDevolucionRepository.save(comprobante);
        guardado.setNumeroComprobante(generarNumeroComprobante(guardado.getId()));

        return comprobanteDevolucionRepository.save(guardado);
    }

    private ComprobanteDevolucionFirma crearFirma(
            ComprobanteDevolucion comprobante,
            RolComprobanteDevolucionFirma rol,
            User usuario,
            LocalDateTime fechaFirma
    ) {
        if (usuario == null) {
            throw new ValidationException(List.of(
                    "No se pudo generar firma " + rol + " para el comprobante"
            ));
        }

        ComprobanteDevolucionFirma firma = new ComprobanteDevolucionFirma();
        firma.setComprobanteDevolucion(comprobante);
        firma.setRolFirma(rol);
        firma.setCodigoUsuarioSnapshot(usuario.getIdentUsuario());
        firma.setNombreUsuarioSnapshot(nombreCompleto(usuario));
        firma.setDniSnapshot(usuario.getDni());
        firma.setFechaFirma(fechaFirma != null ? fechaFirma : LocalDateTime.now());
        return firma;
    }

    private void validarSolicitudDevuelta(SolicitudItems solicitud) {
        if (solicitud.getEstado() != EstadoSolicitudItems.DEVUELTA) {
            throw new ValidationException(List.of(
                    "Solo se puede generar comprobante para solicitudes devueltas"
            ));
        }

        if (solicitud.getUsuarioDevolucion() == null || solicitud.getFechaDevolucion() == null) {
            throw new ValidationException(List.of(
                    "La solicitud devuelta no tiene datos completos de devolucion"
            ));
        }
    }

    private Item obtenerItemDesdeMovimiento(MovimientoInventario movimiento) {
        if (movimiento.getInventarioServicioDestino() != null) {
            return movimiento.getInventarioServicioDestino().getItem();
        }
        throw new ValidationException(List.of(
                "No se pudo identificar el item devuelto en el movimiento " + movimiento.getId()
        ));
    }

    private SolicitudItems obtenerSolicitudPorId(Long solicitudId) {
        return solicitudItemsRepository.findById(solicitudId)
                .orElseThrow(() -> new ValidationException(List.of("Solicitud no encontrada")));
    }

    private ComprobanteDevolucion obtenerComprobantePorId(Long comprobanteId) {
        return comprobanteDevolucionRepository.findById(comprobanteId)
                .orElseThrow(() -> new ValidationException(List.of("Comprobante de devolucion no encontrado")));
    }

    private ComprobanteDevolucion obtenerComprobantePorSolicitudId(Long solicitudId) {
        return comprobanteDevolucionRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ValidationException(List.of("Comprobante de devolucion no encontrado para la solicitud")));
    }

    private String generarNumeroComprobante(Long id) {
        return "CD-" + String.format("%06d", id);
    }

    private String nombreCompleto(User usuario) {
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
        return (nombres + " " + apellidos).trim();
    }

    private void agregarEncabezado(Document document, ComprobanteDevolucion comprobante) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titulo = new Paragraph("COMPROBANTE DE DEVOLUCION", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph codigo = new Paragraph("Formato: ELEMEC-002    Nro Comprobante: " + comprobante.getNumeroComprobante(), normal);
        codigo.setSpacingAfter(8f);
        document.add(codigo);
    }

    private void agregarDatosGenerales(Document document, ComprobanteDevolucion comprobante) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{2f, 4f, 2f, 4f});

        agregarCelda(table, "Cuadrilla", true);
        agregarCelda(table, reemplazarNuloPorVacio(comprobante.getSolicitud().getCuadrilla().getCodigoCuadrilla()), false);
        agregarCelda(table, "Fecha", true);
        agregarCelda(table, formatearFechaHora(comprobante.getFechaGeneracion()), false);

        agregarCelda(table, "Solicitante", true);
        agregarCelda(table, reemplazarNuloPorVacio(nombreCompleto(comprobante.getSolicitud().getSolicitante())), false);
        agregarCelda(table, "Estado Solicitud", true);
        agregarCelda(table, reemplazarNuloPorVacio(comprobante.getSolicitud().getEstado().name()), false);

        if (comprobante.getSolicitud().getObservacionesDevolucion() != null) {
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
            agregarCelda(table, "Obs. Devolucion", true);
            PdfPCell obs = new PdfPCell(new Phrase(reemplazarNuloPorVacio(comprobante.getSolicitud().getObservacionesDevolucion()), normal));
            obs.setColspan(3);
            table.addCell(obs);
        }

        document.add(table);
    }

    private void agregarTablaDetalles(Document document, ComprobanteDevolucion comprobante) throws DocumentException {
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 8);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12f);
        table.setWidths(new float[]{1f, 2.2f, 4.2f, 1.4f, 3.2f});

        agregarCelda(table, "ITEM", true);
        agregarCelda(table, "CODIGO", true);
        agregarCelda(table, "DESCRIPCION", true);
        agregarCelda(table, "CANT DEV.", true);
        agregarCelda(table, "OBS.", true);

        comprobante.getDetalles().stream()
                .sorted(Comparator.comparing(ComprobanteDevolucionDetalle::getNumeroItem))
                .forEach(detalle -> {
                    table.addCell(new Phrase(String.valueOf(detalle.getNumeroItem()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getCodigoItemSnapshot()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getNombreItemSnapshot()), small));
                    table.addCell(new Phrase(String.valueOf(detalle.getCantidadDevuelta()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getEstadoObservacion()), small));
                });

        document.add(table);
    }

    private void agregarFirmas(Document document, ComprobanteDevolucion comprobante) throws DocumentException {
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f});

        ComprobanteDevolucionFirma jefe = comprobante.getFirmas().stream()
                .filter(f -> f.getRolFirma() == RolComprobanteDevolucionFirma.JEFE_CUADRILLA)
                .findFirst()
                .orElse(null);
        ComprobanteDevolucionFirma logistica = comprobante.getFirmas().stream()
                .filter(f -> f.getRolFirma() == RolComprobanteDevolucionFirma.LOGISTICA)
                .findFirst()
                .orElse(null);

        table.addCell(celdaFirma("JEFE CUADRILLA", jefe, normal));
        table.addCell(celdaFirma("LOGISTICA", logistica, normal));

        document.add(table);
    }

    private PdfPCell celdaFirma(String titulo, ComprobanteDevolucionFirma firma, Font font) {
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
