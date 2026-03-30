package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.ProveedorCreateRequest;
import com.pilli3800.inventario.data.dto.request.ProveedorSearchRequest;
import com.pilli3800.inventario.data.dto.request.ProveedorUpdateRequest;
import com.pilli3800.inventario.data.dto.response.ProveedorDto;
import com.pilli3800.inventario.data.models.Proveedor;
import com.pilli3800.inventario.repository.ProveedorRepository;
import com.pilli3800.inventario.specifications.ProveedorSpecifications;
import com.pilli3800.inventario.validator.ProveedorUpdateValidator;
import com.pilli3800.inventario.validator.ProveedorValidator;
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
public class ProveedorService {

    private static final String CODIGO_PREFIX = "PROV";
    private static final int CODIGO_LENGTH = 4;

    private final ProveedorRepository proveedorRepository;
    private final ProveedorValidator proveedorValidator;
    private final ProveedorUpdateValidator proveedorUpdateValidator;

    public ProveedorDto getProveedor(String codigo) {
        return proveedorRepository.findByCodigo(codigo)
                .map(ProveedorDto::from)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    public Page<ProveedorDto> getProveedores(ProveedorSearchRequest request, Pageable pageable) {
        Specification<Proveedor> spec = ProveedorSpecifications.search(request);
        Page<Proveedor> page = proveedorRepository.findAll(spec, pageable);
        return page.map(ProveedorDto::from);
    }

    public ProveedorDto createProveedor(ProveedorCreateRequest request) {

        proveedorValidator.validate(request);

        Proveedor proveedor = new Proveedor();
        proveedor.setCodigo(generarCodigo());
        proveedor.setRuc(request.ruc());
        proveedor.setNombre(request.nombre());
        proveedor.setEmail(request.email());
        proveedor.setTelefono(request.telefono());
        proveedor.setObservaciones(request.observaciones());
        proveedor.setEnabled(true);

        return ProveedorDto.from(proveedorRepository.save(proveedor));
    }

    public ProveedorDto updateProveedor(String codigo, ProveedorUpdateRequest request) {

        Proveedor proveedor = proveedorRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        proveedorUpdateValidator.validate(proveedor.getId(), request);

        if (request.ruc() != null) {
            proveedor.setRuc(request.ruc());
        }

        if (request.nombre() != null) {
            proveedor.setNombre(request.nombre());
        }

        if (request.email() != null) {
            proveedor.setEmail(request.email());
        }

        if (request.telefono() != null) {
            proveedor.setTelefono(request.telefono());
        }

        if (request.observaciones() != null) {
            proveedor.setObservaciones(request.observaciones());
        }

        if (request.enabled() != null) {
            proveedor.setEnabled(request.enabled());
        }

        return ProveedorDto.from(proveedorRepository.save(proveedor));
    }

    public void enableProveedor(String codigo) {
        setProveedorEnabled(codigo, true);
    }

    public void disableProveedor(String codigo) {
        setProveedorEnabled(codigo, false);
    }

    private void setProveedorEnabled(String codigo, boolean enabled) {
        Proveedor proveedor = proveedorRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        proveedor.setEnabled(enabled);
        proveedorRepository.save(proveedor);
    }

    private String generarCodigo() {
        // Codigo incremental con prefijo PROV y 4 digitos
        return proveedorRepository.findTopByOrderByIdDesc()
                .map(Proveedor::getCodigo)
                .map(this::incrementarCodigo)
                .orElse(CODIGO_PREFIX + "0001");
    }

    private String incrementarCodigo(String codigo) {
        String parteNumerica = codigo.replace(CODIGO_PREFIX, "");
        int numero = Integer.parseInt(parteNumerica) + 1;
        return CODIGO_PREFIX + String.format("%0" + CODIGO_LENGTH + "d", numero);
    }

    public byte[] exportProveedoresToExcel(ProveedorSearchRequest request) throws IOException {

        Specification<Proveedor> spec = ProveedorSpecifications.search(request);
        List<Proveedor> proveedores = proveedorRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Proveedores");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {
                "ID",
                "Codigo",
                "Ruc",
                "Nombre",
                "Email",
                "Telefono",
                "Activo",
                "Observaciones"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Proveedor proveedor : proveedores) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(proveedor.getId());
            row.createCell(1).setCellValue(proveedor.getCodigo());
            row.createCell(2).setCellValue(proveedor.getRuc());
            row.createCell(3).setCellValue(proveedor.getNombre());
            row.createCell(4).setCellValue(proveedor.getEmail());
            row.createCell(5).setCellValue(proveedor.getTelefono());
            row.createCell(6).setCellValue(proveedor.isEnabled());
            row.createCell(7).setCellValue(proveedor.getObservaciones());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] exportProveedoresToExcelAuditoria(ProveedorSearchRequest request) throws IOException {

        Specification<Proveedor> spec = ProveedorSpecifications.search(request);
        List<Proveedor> proveedores = proveedorRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Proveedores Auditoria");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {
                "ID",
                "Codigo",
                "Ruc",
                "Nombre",
                "Email",
                "Telefono",
                "Activo",
                "Observaciones",
                "Fecha Creacion",
                "Usuario Creacion",
                "Fecha Actualizacion",
                "Usuario Actualizacion"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Proveedor proveedor : proveedores) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(proveedor.getId());
            row.createCell(1).setCellValue(proveedor.getCodigo());
            row.createCell(2).setCellValue(proveedor.getRuc());
            row.createCell(3).setCellValue(proveedor.getNombre());
            row.createCell(4).setCellValue(proveedor.getEmail());
            row.createCell(5).setCellValue(proveedor.getTelefono());
            row.createCell(6).setCellValue(proveedor.isEnabled());
            row.createCell(7).setCellValue(proveedor.getObservaciones());

            row.createCell(8).setCellValue(
                    proveedor.getFcCreacion() != null
                            ? proveedor.getFcCreacion().toString()
                            : ""
            );
            row.createCell(9).setCellValue(
                    proveedor.getUsuCreacion() != null
                            ? proveedor.getUsuCreacion()
                            : ""
            );
            row.createCell(10).setCellValue(
                    proveedor.getFcActualizacion() != null
                            ? proveedor.getFcActualizacion().toString()
                            : ""
            );
            row.createCell(11).setCellValue(
                    proveedor.getUsuActualizacion() != null
                            ? proveedor.getUsuActualizacion()
                            : ""
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
