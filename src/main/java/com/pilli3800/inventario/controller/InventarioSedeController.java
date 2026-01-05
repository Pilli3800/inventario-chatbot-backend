package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.InventarioSedeCreateRequest;
import com.pilli3800.inventario.data.dto.request.InventarioSedeSearchRequest;
import com.pilli3800.inventario.data.dto.response.InventarioSedeDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.InventarioSedeService;
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
@RequestMapping("/api/logistica/inventarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LOGISTICA')")
public class InventarioSedeController {

    private final InventarioSedeService inventarioSedeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<InventarioSedeDto> getInventario(
            @RequestParam String sedeCodigo,
            @RequestParam(required = false) String nombreItem,
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) TipoItem tipoItem,
            @RequestParam(required = false) Boolean enabledItem,
            @RequestParam(required = false) Boolean conStock,
            @PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable
    ) {

        InventarioSedeSearchRequest request =
                new InventarioSedeSearchRequest(
                        sedeCodigo,
                        nombreItem,
                        codigoItem,
                        tipoItem,
                        enabledItem,
                        conStock
                );

        Page<InventarioSedeDto> page =
                inventarioSedeService.getInventario(request, pageable);

        return PageResponse.from(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<String> asignarItemASede(
            @Valid @RequestBody InventarioSedeCreateRequest request
    ) {
        inventarioSedeService.asignarItemASede(request);
        return new SingleResponse<>(
                201,
                "/api/logistica/inventarios",
                "Item asignado a la sede correctamente"
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> eliminarAsignacion(@PathVariable Long id) {
        inventarioSedeService.eliminarAsignacion(id);
        return new SingleResponse<>(
                200,
                "/api/logistica/inventarios/" + id,
                "Asignación eliminada correctamente"
        );
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportInventarioExcel(
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

        byte[] excel = inventarioSedeService.exportInventarioToExcel(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "inventario_" + sedeCodigo + "_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

}