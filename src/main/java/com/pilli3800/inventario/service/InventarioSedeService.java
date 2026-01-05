package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.InventarioSedeCreateRequest;
import com.pilli3800.inventario.data.dto.request.InventarioSedeSearchRequest;
import com.pilli3800.inventario.data.dto.response.InventarioSedeDto;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.repository.InventarioSedeRepository;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.repository.SedeRepository;
import com.pilli3800.inventario.specifications.InventarioSedeSpecifications;
import com.pilli3800.inventario.validator.InventarioSedeDeleteValidator;
import com.pilli3800.inventario.validator.InventarioSedeValidator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioSedeService {

    private final InventarioSedeRepository inventarioSedeRepository;
    private final ItemRepository itemRepository;
    private final SedeRepository sedeRepository;
    private final InventarioSedeValidator inventarioSedeValidator;
    private final InventarioSedeDeleteValidator inventarioSedeDeleteValidator;

    public Page<InventarioSedeDto> getInventario(InventarioSedeSearchRequest request, Pageable pageable) {

        Specification<InventarioSede> spec =
                InventarioSedeSpecifications.search(request);

        return inventarioSedeRepository
                .findAll(spec, pageable)
                .map(InventarioSedeDto::from);
    }

    public void asignarItemASede(InventarioSedeCreateRequest request) {

        inventarioSedeValidator.validate(request);

        Item item = itemRepository.findByCodigoItem(request.codigoItem()).get();
        Sede sede = sedeRepository.findByCodigo(request.sedeCodigo()).get();

        InventarioSede inventario = new InventarioSede();
        inventario.setItem(item);
        inventario.setSede(sede);
        // stock = 0 por defecto

        inventarioSedeRepository.save(inventario);
    }

    public void eliminarAsignacion(Long inventarioSedeId) {

        InventarioSede inventario = inventarioSedeRepository
                .findById(inventarioSedeId)
                .orElseThrow(() -> new RuntimeException("Asignacion no encontrada"));

        inventarioSedeDeleteValidator.validate(inventario);

        inventarioSedeRepository.delete(inventario);
    }

    public byte[] exportInventarioToExcel(
            InventarioSedeSearchRequest request
    ) throws IOException {

        List<InventarioSede> registros =
                inventarioSedeRepository.findAll(
                        InventarioSedeSpecifications.search(request)
                );

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {
                "ID",
                "Sede",
                "Código Item",
                "Nombre Item",
                "Tipo",
                "Stock"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (InventarioSede inv : registros) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(inv.getId());
            row.createCell(1).setCellValue(inv.getSede().getCodigo());
            row.createCell(2).setCellValue(inv.getItem().getCodigoItem());
            row.createCell(3).setCellValue(inv.getItem().getNombre());
            row.createCell(4).setCellValue(inv.getItem().getTipo().name());
            row.createCell(5).setCellValue(inv.getStock());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] exportInventarioToExcelAuditoria(InventarioSedeSearchRequest request) throws IOException {

        Specification<InventarioSede> spec = InventarioSedeSpecifications.search(request);
        List<InventarioSede> inventarios = inventarioSedeRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario Auditoría");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {
                "ID",
                "Sede",
                "Código Item",
                "Nombre Item",
                "Tipo",
                "Stock",
                "Fecha Creación",
                "Usuario Creación",
                "Fecha Actualización",
                "Usuario Actualización"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (InventarioSede inv : inventarios) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(inv.getId());
            row.createCell(1).setCellValue(inv.getSede().getCodigo());
            row.createCell(2).setCellValue(inv.getItem().getCodigoItem());
            row.createCell(3).setCellValue(inv.getItem().getNombre());
            row.createCell(4).setCellValue(inv.getItem().getTipo().name());
            row.createCell(5).setCellValue(inv.getStock());

            row.createCell(6).setCellValue(
                    inv.getFcCreacion() != null ? inv.getFcCreacion().toString() : ""
            );
            row.createCell(7).setCellValue(
                    inv.getUsuCreacion() != null ? inv.getUsuCreacion() : ""
            );
            row.createCell(8).setCellValue(
                    inv.getFcActualizacion() != null ? inv.getFcActualizacion().toString() : ""
            );
            row.createCell(9).setCellValue(
                    inv.getUsuActualizacion() != null ? inv.getUsuActualizacion() : ""
            );
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

}