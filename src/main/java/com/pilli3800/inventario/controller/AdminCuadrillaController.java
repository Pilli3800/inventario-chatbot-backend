package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.InventarioSedeSearchRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaCreateRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaSearchRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaUpdateRequest;
import com.pilli3800.inventario.data.dto.response.CuadrillaDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.CuadrillaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/admin/cuadrillas")
@PreAuthorize("hasAnyRole('ADMINISTRACION', 'GERENCIA')")
@RequiredArgsConstructor
public class AdminCuadrillaController {

    private final CuadrillaService cuadrillaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<CuadrillaDto> createCuadrilla(
            @Valid @RequestBody CuadrillaCreateRequest request) {
        return new SingleResponse<>(
                201,
                "/api/logistica/cuadrillas",
                cuadrillaService.createCuadrilla(request)
        );
    }

    @PutMapping("/{codigoCuadrilla}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<CuadrillaDto> updateCuadrilla(
            @PathVariable String codigoCuadrilla,
            @RequestBody CuadrillaUpdateRequest request
    ) {
        return new SingleResponse<>(
                200,
                "/api/admin/cuadrillas/" + codigoCuadrilla,
                cuadrillaService.updateCuadrilla(codigoCuadrilla, request)
        );
    }

    @PatchMapping("/{codigoCuadrilla}/desactivar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> disableItem(@PathVariable String codigoCuadrilla) {
        cuadrillaService.disableItem(codigoCuadrilla);
        return new SingleResponse<>(
                200,
                "/api/admin/cuadrillas/" + codigoCuadrilla + "/desactivar",
                "Cuadrilla desactivada"
        );
    }

    @PatchMapping("/{codigoCuadrilla}/activar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> enableItem(@PathVariable String codigoCuadrilla) {
        cuadrillaService.enableItem(codigoCuadrilla);
        return new SingleResponse<>(
                200,
                "/api/admin/cuadrillas/" + codigoCuadrilla + "/activar",
                "Cuadrilla activada"
        );
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportCuadrillasExcel(
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) String identUsuarioJefe,
            @RequestParam(required = false) Boolean enabled
    ) throws IOException {

        CuadrillaSearchRequest request =
                new CuadrillaSearchRequest(
                        codigoCuadrilla,
                        identUsuarioJefe,
                        enabled
                );

        byte[] excel = cuadrillaService.exportCuadrillasToExcel(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "cuadrillas_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

    @GetMapping("/export/excel/auditoria")
    public ResponseEntity<byte[]> exportCuadrillasAuditoria(
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) String identUsuarioJefe,
            @RequestParam(required = false) Boolean enabled
    ) throws IOException {

        CuadrillaSearchRequest request =
                new CuadrillaSearchRequest(
                        codigoCuadrilla,
                        identUsuarioJefe,
                        enabled
                );

        byte[] excel = cuadrillaService.exportCuadrillasToExcelAuditoria(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "cuadrillas_auditoria_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

}