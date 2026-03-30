package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.ProveedorCreateRequest;
import com.pilli3800.inventario.data.dto.request.ProveedorSearchRequest;
import com.pilli3800.inventario.data.dto.request.ProveedorUpdateRequest;
import com.pilli3800.inventario.data.dto.response.ProveedorDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/logistica/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping("/{codigoProveedor}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ProveedorDto> getProveedor(@PathVariable String codigoProveedor) {
        return new SingleResponse<>(
                200,
                "/api/logistica/proveedores/" + codigoProveedor,
                proveedorService.getProveedor(codigoProveedor)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ProveedorDto> getProveedores(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String ruc,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(page = 0, size = 5, sort = "nombre") Pageable pageable
    ) {
        ProveedorSearchRequest request = new ProveedorSearchRequest(
                codigo, ruc, nombre, enabled
        );

        return PageResponse.from(
                proveedorService.getProveedores(request, pageable)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<ProveedorDto> createProveedor(
            @Valid @RequestBody ProveedorCreateRequest request
    ) {
        return new SingleResponse<>(
                201,
                "/api/logistica/proveedores",
                proveedorService.createProveedor(request)
        );
    }

    @PutMapping("/{codigoProveedor}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ProveedorDto> updateProveedor(
            @PathVariable String codigoProveedor,
            @Valid @RequestBody ProveedorUpdateRequest request
    ) {
        return new SingleResponse<>(
                200,
                "/api/logistica/proveedores/" + codigoProveedor,
                proveedorService.updateProveedor(codigoProveedor, request)
        );
    }

    @PatchMapping("/{codigoProveedor}/desactivar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> disableProveedor(@PathVariable String codigoProveedor) {
        proveedorService.disableProveedor(codigoProveedor);
        return new SingleResponse<>(
                200,
                "/api/logistica/proveedores/" + codigoProveedor + "/desactivar",
                "Proveedor desactivado"
        );
    }

    @PatchMapping("/{codigoProveedor}/activar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> enableProveedor(@PathVariable String codigoProveedor) {
        proveedorService.enableProveedor(codigoProveedor);
        return new SingleResponse<>(
                200,
                "/api/logistica/proveedores/" + codigoProveedor + "/activar",
                "Proveedor activado"
        );
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportProveedoresExcel(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String ruc,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean enabled
    ) throws IOException {

        ProveedorSearchRequest request = new ProveedorSearchRequest(
                codigo, ruc, nombre, enabled
        );

        byte[] excel = proveedorService.exportProveedoresToExcel(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "proveedores_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

    @GetMapping("/export/excel/auditoria")
    @PreAuthorize("hasAnyRole('GERENCIA', 'ADMINISTRACION')")
    public ResponseEntity<byte[]> exportProveedoresExcelAuditoria(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String ruc,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean enabled
    ) throws IOException {

        ProveedorSearchRequest request = new ProveedorSearchRequest(
                codigo, ruc, nombre, enabled
        );

        byte[] excel = proveedorService.exportProveedoresToExcelAuditoria(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "proveedores_auditoria_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}
