package com.pilli3800.inventario.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoCreateRequest;
import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoDetalleRequest;
import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoSearchRequest;
import com.pilli3800.inventario.data.dto.response.ConteoFisicoDto;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.conteofisico.ConteoFisico;
import com.pilli3800.inventario.data.models.conteofisico.ConteoFisicoDetalle;
import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.*;
import com.pilli3800.inventario.specifications.ConteoFisicoSpecifications;
import com.pilli3800.inventario.util.TextNormalizer;
import com.pilli3800.inventario.validator.ConteoFisicoCreateValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pilli3800.inventario.util.DateFormat.formatearFechaHora;
import static com.pilli3800.inventario.util.TextFormat.reemplazarNuloPorVacio;

@Service
@RequiredArgsConstructor
public class ConteoFisicoService {

    private final ConteoFisicoRepository conteoFisicoRepository;
    private final ConteoFisicoCreateValidator createValidator;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SedeRepository sedeRepository;
    private final ServicioRepository servicioRepository;
    private final ItemRepository itemRepository;
    private final InventarioSedeRepository inventarioSedeRepository;
    private final InventarioServicioRepository inventarioServicioRepository;

    @Transactional
    public ConteoFisicoDto crearConteo(ConteoFisicoCreateRequest request) {
        createValidator.validate(request);

        User usuario = obtenerUsuarioAutenticado();
        String codigoUbicacion = TextNormalizer.normalizeCode(request.codigoUbicacion());

        ConteoFisico conteo = new ConteoFisico();
        conteo.setTipoInventario(request.tipoInventario());
        conteo.setUsuario(usuario);
        conteo.setFechaConteo(LocalDateTime.now());
        conteo.setObservaciones(request.observaciones());

        if (request.tipoInventario() == TipoInventarioConteo.SEDE) {
            conteo.setSede(obtenerSede(codigoUbicacion));
        } else {
            conteo.setServicio(obtenerServicio(codigoUbicacion));
        }

        for (ConteoFisicoDetalleRequest detalleRequest : request.detalles()) {
            String codigoItem = TextNormalizer.normalizeCode(detalleRequest.codigoItem());
            Item item = obtenerItem(codigoItem);
            Long stockActual = obtenerStockActual(request.tipoInventario(), conteo, item);

            if (!stockActual.equals(detalleRequest.stockSistema())) {
                throw new ValidationException(
                        List.of("El stock del sistema no coincide para el item: "
                                + codigoItem
                                + " (enviado: " + detalleRequest.stockSistema()
                                + ", actual: " + stockActual + ")")
                );
            }

            ConteoFisicoDetalle detalle = new ConteoFisicoDetalle();
            detalle.setConteo(conteo);
            detalle.setItem(item);
            detalle.setStockSistema(detalleRequest.stockSistema());
            detalle.setCantidadFisica(detalleRequest.cantidadFisica());
            detalle.setDiferencia(detalleRequest.cantidadFisica() - detalleRequest.stockSistema());
            detalle.setObservacion(detalleRequest.observacion());
            conteo.getDetalles().add(detalle);
        }

        return ConteoFisicoDto.resumen(conteoFisicoRepository.save(conteo));
    }

    @Transactional
    public Page<ConteoFisicoDto> getConteos(
            ConteoFisicoSearchRequest request,
            Pageable pageable
    ) {
        ConteoFisicoSearchRequest requestAjustado = ajustarFiltroUsuario(request);
        ConteoFisicoSearchRequest requestNormalizado = normalizarFiltroUbicacion(requestAjustado);
        Specification<ConteoFisico> spec = ConteoFisicoSpecifications.search(requestNormalizado);

        return conteoFisicoRepository
                .findAll(spec, pageable)
                .map(ConteoFisicoDto::resumen);
    }

    @Transactional
    public ConteoFisicoDto getConteo(Long id) {
        return ConteoFisicoDto.from(obtenerConteoPorId(id));
    }

    @Transactional
    public byte[] generarPdfConteo(Long id) {
        ConteoFisico conteo = obtenerConteoPorId(id);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            agregarEncabezado(document, conteo);
            agregarDatosGenerales(document, conteo);
            agregarTablaDetalles(document, conteo);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ValidationException(List.of("No se pudo generar el PDF del conteo fisico"));
        }
    }

    @Transactional
    public byte[] generarExcelConteo(Long id) {
        ConteoFisico conteo = obtenerConteoPorId(id);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Conteo Fisico");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIdx = 0;
            Row title = sheet.createRow(rowIdx++);
            Cell titleCell = title.createCell(0);
            titleCell.setCellValue("Reporte de Conteo Fisico");
            titleCell.setCellStyle(headerStyle);

            rowIdx++;
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Nro Reporte", "CF-" + String.format("%06d", conteo.getId()), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Tipo Inventario", conteo.getTipoInventario().name(), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Codigo Ubicacion", codigoUbicacion(conteo), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Ubicacion", nombreUbicacion(conteo), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Fecha Conteo", formatearFechaHora(conteo.getFechaConteo()), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Codigo Usuario", conteo.getUsuario().getIdentUsuario(), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Usuario", nombreCompleto(conteo.getUsuario()), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Total Items", String.valueOf(conteo.getDetalles().size()), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Items Con Discrepancia", String.valueOf(contarDiscrepancias(conteo)), headerStyle);
            rowIdx = agregarFilaResumen(sheet, rowIdx, "Observaciones", reemplazarNuloPorVacio(conteo.getObservaciones()), headerStyle);

            rowIdx++;
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {
                    "Item",
                    "Codigo Item",
                    "Nombre Item",
                    "Stock Sistema",
                    "Cantidad Fisica",
                    "Diferencia",
                    "Observacion"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int numero = 1;
            List<ConteoFisicoDetalle> detalles = conteo.getDetalles().stream()
                    .sorted(Comparator.comparing(detalle -> detalle.getItem().getCodigoItem()))
                    .toList();

            for (ConteoFisicoDetalle detalle : detalles) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(numero++);
                row.createCell(1).setCellValue(reemplazarNuloPorVacio(detalle.getItem().getCodigoItem()));
                row.createCell(2).setCellValue(reemplazarNuloPorVacio(detalle.getItem().getNombre()));
                row.createCell(3).setCellValue(detalle.getStockSistema());
                row.createCell(4).setCellValue(detalle.getCantidadFisica());
                row.createCell(5).setCellValue(detalle.getDiferencia());
                row.createCell(6).setCellValue(reemplazarNuloPorVacio(detalle.getObservacion()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ValidationException(List.of("No se pudo generar el Excel del conteo fisico"));
        }
    }

    private ConteoFisicoSearchRequest ajustarFiltroUsuario(ConteoFisicoSearchRequest request) {
        if (request.usuario() == null || request.usuario().isBlank()) {
            return request;
        }

        User usuario = userRepository
                .findByIdentUsuario(request.usuario())
                .orElseThrow(() -> new ValidationException(
                        List.of("Usuario no encontrado")
                ));

        return new ConteoFisicoSearchRequest(
                request.fechaDesde(),
                request.fechaHasta(),
                request.usuario(),
                request.tipoInventario(),
                request.codigoUbicacion(),
                usuario.getId()
        );
    }

    private ConteoFisicoSearchRequest normalizarFiltroUbicacion(ConteoFisicoSearchRequest request) {
        String codigoUbicacion = request.codigoUbicacion() != null && !request.codigoUbicacion().isBlank()
                ? TextNormalizer.normalizeCode(request.codigoUbicacion())
                : null;

        return new ConteoFisicoSearchRequest(
                request.fechaDesde(),
                request.fechaHasta(),
                request.usuario(),
                request.tipoInventario(),
                codigoUbicacion,
                request.usuarioId()
        );
    }

    private ConteoFisico obtenerConteoPorId(Long id) {
        return conteoFisicoRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException(
                        List.of("Conteo fisico no encontrado")
                ));
    }

    private User obtenerUsuarioAutenticado() {
        String identUsuario = userService.getIdentUsuario();
        if (identUsuario == null) {
            throw new ValidationException(List.of("Usuario no autenticado"));
        }
        return userRepository
                .findByIdentUsuario(identUsuario)
                .orElseThrow(() -> new ValidationException(
                        List.of("Usuario autenticado no encontrado")
                ));
    }

    private Sede obtenerSede(String codigoUbicacion) {
        Sede sede = sedeRepository
                .findByCodigo(codigoUbicacion)
                .orElseThrow(() -> new ValidationException(
                        List.of("La sede no existe")
                ));

        if (!sede.isEnabled()) {
            throw new ValidationException(List.of("La sede esta desactivada"));
        }

        return sede;
    }

    private Servicio obtenerServicio(String codigoUbicacion) {
        Servicio servicio = servicioRepository
                .findByCodigo(codigoUbicacion)
                .orElseThrow(() -> new ValidationException(
                        List.of("El servicio no existe")
                ));

        if (!servicio.isEnabled()) {
            throw new ValidationException(List.of("El servicio esta desactivado"));
        }

        return servicio;
    }

    private Item obtenerItem(String codigoItem) {
        Item item = itemRepository
                .findByCodigoItem(codigoItem)
                .orElseThrow(() -> new ValidationException(
                        List.of("El item no existe: " + codigoItem)
                ));

        if (!item.isEnabled()) {
            throw new ValidationException(List.of("El item esta desactivado: " + codigoItem));
        }

        return item;
    }

    private Long obtenerStockActual(
            TipoInventarioConteo tipoInventario,
            ConteoFisico conteo,
            Item item
    ) {
        if (tipoInventario == TipoInventarioConteo.SEDE) {
            InventarioSede inventario = inventarioSedeRepository
                    .findByItemIdAndSedeId(item.getId(), conteo.getSede().getId())
                    .orElseThrow(() -> new ValidationException(
                            List.of("El item no esta asignado al inventario de la sede: "
                                    + item.getCodigoItem())
                    ));
            return inventario.getStock();
        }

        InventarioServicio inventario = inventarioServicioRepository
                .findByServicioIdAndItemId(conteo.getServicio().getId(), item.getId())
                .orElseThrow(() -> new ValidationException(
                        List.of("El item no esta asignado al inventario del servicio: "
                                + item.getCodigoItem())
                ));
        return inventario.getStockActual();
    }

    private void agregarEncabezado(Document document, ConteoFisico conteo) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph titulo = new Paragraph("REPORTE DE CONTEO FISICO", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph codigo = new Paragraph("Nro Reporte: CF-" + String.format("%06d", conteo.getId()), normal);
        codigo.setSpacingAfter(8f);
        document.add(codigo);
    }

    private void agregarDatosGenerales(Document document, ConteoFisico conteo) throws DocumentException {
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{2f, 4f, 2f, 4f});

        agregarCelda(table, "Tipo Inventario", true);
        agregarCelda(table, conteo.getTipoInventario().name(), false);
        agregarCelda(table, "Fecha Conteo", true);
        agregarCelda(table, formatearFechaHora(conteo.getFechaConteo()), false);

        agregarCelda(table, "Codigo Ubicacion", true);
        agregarCelda(table, codigoUbicacion(conteo), false);
        agregarCelda(table, "Ubicacion", true);
        agregarCelda(table, nombreUbicacion(conteo), false);

        agregarCelda(table, "Usuario", true);
        agregarCelda(table, nombreCompleto(conteo.getUsuario()), false);
        agregarCelda(table, "Codigo Usuario", true);
        agregarCelda(table, conteo.getUsuario().getIdentUsuario(), false);

        agregarCelda(table, "Total Items", true);
        agregarCelda(table, String.valueOf(conteo.getDetalles().size()), false);
        agregarCelda(table, "Con Discrepancia", true);
        agregarCelda(table, String.valueOf(contarDiscrepancias(conteo)), false);

        if (conteo.getObservaciones() != null && !conteo.getObservaciones().isBlank()) {
            agregarCelda(table, "Observaciones", true);
            PdfPCell obs = new PdfPCell(new Phrase(reemplazarNuloPorVacio(conteo.getObservaciones()), normal));
            obs.setColspan(3);
            table.addCell(obs);
        }

        document.add(table);
    }

    private void agregarTablaDetalles(Document document, ConteoFisico conteo) throws DocumentException {
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 8);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12f);
        table.setWidths(new float[]{0.8f, 2f, 4f, 1.6f, 1.6f, 1.6f, 4f});

        agregarCelda(table, "ITEM", true);
        agregarCelda(table, "CODIGO", true);
        agregarCelda(table, "NOMBRE", true);
        agregarCelda(table, "STOCK SIST.", true);
        agregarCelda(table, "CANT. FISICA", true);
        agregarCelda(table, "DIF.", true);
        agregarCelda(table, "OBSERVACION", true);

        AtomicInteger numero = new AtomicInteger(1);
        conteo.getDetalles().stream()
                .sorted(Comparator.comparing(detalle -> detalle.getItem().getCodigoItem()))
                .forEach(detalle -> {
                    table.addCell(new Phrase(String.valueOf(numero.getAndIncrement()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getItem().getCodigoItem()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getItem().getNombre()), small));
                    table.addCell(new Phrase(String.valueOf(detalle.getStockSistema()), small));
                    table.addCell(new Phrase(String.valueOf(detalle.getCantidadFisica()), small));
                    table.addCell(new Phrase(String.valueOf(detalle.getDiferencia()), small));
                    table.addCell(new Phrase(reemplazarNuloPorVacio(detalle.getObservacion()), small));
                });

        document.add(table);
    }

    private void agregarCelda(PdfPTable table, String valor, boolean encabezado) {
        com.lowagie.text.Font font = encabezado
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)
                : FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(reemplazarNuloPorVacio(valor), font));
        table.addCell(cell);
    }

    private int agregarFilaResumen(
            Sheet sheet,
            int rowIdx,
            String etiqueta,
            String valor,
            CellStyle headerStyle
    ) {
        Row row = sheet.createRow(rowIdx++);
        Cell label = row.createCell(0);
        label.setCellValue(etiqueta);
        label.setCellStyle(headerStyle);
        row.createCell(1).setCellValue(reemplazarNuloPorVacio(valor));
        return rowIdx;
    }

    private String codigoUbicacion(ConteoFisico conteo) {
        return conteo.getTipoInventario() == TipoInventarioConteo.SEDE
                ? conteo.getSede().getCodigo()
                : conteo.getServicio().getCodigo();
    }

    private String nombreUbicacion(ConteoFisico conteo) {
        return conteo.getTipoInventario() == TipoInventarioConteo.SEDE
                ? conteo.getSede().getNombre()
                : conteo.getServicio().getNombre();
    }

    private long contarDiscrepancias(ConteoFisico conteo) {
        return conteo.getDetalles()
                .stream()
                .filter(detalle -> detalle.getDiferencia() != 0)
                .count();
    }

    private String nombreCompleto(User usuario) {
        String nombres = usuario.getNombres() != null ? usuario.getNombres() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos() : "";
        return (nombres + " " + apellidos).trim();
    }
}
