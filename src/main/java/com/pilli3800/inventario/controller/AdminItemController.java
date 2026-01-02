package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.ItemSearchRequest;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.ItemService;
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
@RequestMapping("/api/admin/items")
@PreAuthorize("hasRole('ADMINISTRACION')")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    @GetMapping("/export/excel/auditoria")
    public ResponseEntity<byte[]> exportItemsExcelAuditoria(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) TipoItem tipo,
            @RequestParam(required = false) Boolean enabled
    ) throws IOException {

        ItemSearchRequest request = new ItemSearchRequest(
                nombre, codigoItem, tipo, enabled
        );

        byte[] excel = itemService.exportItemsToExcelAuditoria(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "items_auditoria_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}