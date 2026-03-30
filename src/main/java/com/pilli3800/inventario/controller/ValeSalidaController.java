package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.response.ValeSalidaDto;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.ValeSalidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vales-salida")
@RequiredArgsConstructor
public class ValeSalidaController {

    private final ValeSalidaService valeSalidaService;

    @PostMapping("/solicitud/{solicitudId}/generar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<ValeSalidaDto> generarValePorSolicitud(@PathVariable Long solicitudId) {
        return new SingleResponse<>(
                201,
                "/api/vales-salida/solicitud/" + solicitudId + "/generar",
                valeSalidaService.generarValeParaSolicitud(solicitudId)
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ValeSalidaDto> getVale(@PathVariable Long id) {
        return new SingleResponse<>(
                200,
                "/api/vales-salida/" + id,
                valeSalidaService.getVale(id)
        );
    }

    @GetMapping("/solicitud/{solicitudId}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ValeSalidaDto> getValePorSolicitud(@PathVariable Long solicitudId) {
        return new SingleResponse<>(
                200,
                "/api/vales-salida/solicitud/" + solicitudId,
                valeSalidaService.getValePorSolicitud(solicitudId)
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        ValeSalidaDto vale = valeSalidaService.getVale(id);
        byte[] pdf = valeSalidaService.generarPdfVale(id);
        String filename = "vale_salida_" + vale.numeroVale() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
