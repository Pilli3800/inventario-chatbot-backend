package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.InventarioSedeSearchRequest;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.InventarioSedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/inventarios")
@PreAuthorize("hasRole('ADMINISTRACION')")
@RequiredArgsConstructor
public class AdminInventarioSedeController {

    private final InventarioSedeService inventarioSedeService;

    @GetMapping("/export/excel/auditoria")
    public ResponseEntity<byte[]> exportInventarioAuditoria(
            @RequestParam String sedeCodigo,
            @RequestParam(required = false) String nombreItem,
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) TipoItem tipoItem,
            @RequestParam(required = false) Boolean enabledItem,
            @RequestParam(required = false) Boolean conStock
    ) throws IOException {

        InventarioSedeSearchRequest request =
                new InventarioSedeSearchRequest(
                        sedeCodigo,
                        nombreItem,
                        codigoItem,
                        tipoItem,
                        enabledItem,
                        conStock
                );

        byte[] excel = inventarioSedeService.exportInventarioToExcelAuditoria(request);

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "inventario_auditoria_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}
