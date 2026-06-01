package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoCreateRequest;
import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoSearchRequest;
import com.pilli3800.inventario.data.dto.response.ConteoFisicoDashboardDto;
import com.pilli3800.inventario.data.dto.response.ConteoFisicoDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import com.pilli3800.inventario.service.ConteoFisicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/conteos-fisicos")
@RequiredArgsConstructor
public class ConteoFisicoController {

    private final ConteoFisicoService conteoFisicoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<ConteoFisicoDto> crearConteo(
            @Valid @RequestBody ConteoFisicoCreateRequest request
    ) {
        return new SingleResponse<>(
                201,
                "/api/conteos-fisicos",
                conteoFisicoService.crearConteo(request)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ConteoFisicoDto> getConteos(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) TipoInventarioConteo tipoInventario,
            @RequestParam(required = false) String codigoUbicacion,
            @PageableDefault(page = 0, size = 10, sort = "fechaConteo", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        ConteoFisicoSearchRequest request = new ConteoFisicoSearchRequest(
                fechaDesde,
                fechaHasta,
                usuario,
                tipoInventario,
                codigoUbicacion,
                null
        );

        Page<ConteoFisicoDto> page =
                conteoFisicoService.getConteos(request, ajustarOrden(pageable));

        return PageResponse.from(page);
    }

    @GetMapping("/dashboard")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ConteoFisicoDashboardDto> getDashboard(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) TipoInventarioConteo tipoInventario,
            @RequestParam(required = false) String codigoUbicacion
    ) {
        return new SingleResponse<>(
                200,
                "/api/conteos-fisicos/dashboard",
                conteoFisicoService.getDashboard(
                        fechaDesde,
                        fechaHasta,
                        usuario,
                        tipoInventario,
                        codigoUbicacion
                )
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ConteoFisicoDto> getConteo(
            @PathVariable Long id
    ) {
        return new SingleResponse<>(
                200,
                "/api/conteos-fisicos/" + id,
                conteoFisicoService.getConteo(id)
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        byte[] pdf = conteoFisicoService.generarPdfConteo(id);
        String filename = "conteo_fisico_CF-" + String.format("%06d", id) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> descargarExcel(@PathVariable Long id) {
        byte[] excel = conteoFisicoService.generarExcelConteo(id);
        String filename = "conteo_fisico_CF-" + String.format("%06d", id) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

    private Pageable ajustarOrden(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if ("fechaConteo".equalsIgnoreCase(property)) {
                property = "fechaConteo";
            }
            orders.add(new Sort.Order(order.getDirection(), property));
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(orders)
        );
    }
}
