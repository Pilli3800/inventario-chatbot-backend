package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.response.ComprobanteDevolucionDto;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.ComprobanteDevolucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comprobantes-devolucion")
@RequiredArgsConstructor
public class ComprobanteDevolucionController {

    private final ComprobanteDevolucionService comprobanteDevolucionService;

    @PostMapping("/solicitud/{solicitudId}/generar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<ComprobanteDevolucionDto> generarComprobantePorSolicitud(@PathVariable Long solicitudId) {
        return new SingleResponse<>(
                201,
                "/api/comprobantes-devolucion/solicitud/" + solicitudId + "/generar",
                comprobanteDevolucionService.generarComprobanteParaSolicitud(solicitudId)
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ComprobanteDevolucionDto> getComprobante(@PathVariable Long id) {
        return new SingleResponse<>(
                200,
                "/api/comprobantes-devolucion/" + id,
                comprobanteDevolucionService.getComprobante(id)
        );
    }

    @GetMapping("/solicitud/{solicitudId}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ComprobanteDevolucionDto> getComprobantePorSolicitud(@PathVariable Long solicitudId) {
        return new SingleResponse<>(
                200,
                "/api/comprobantes-devolucion/solicitud/" + solicitudId,
                comprobanteDevolucionService.getComprobantePorSolicitud(solicitudId)
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        ComprobanteDevolucionDto comprobante = comprobanteDevolucionService.getComprobante(id);
        byte[] pdf = comprobanteDevolucionService.generarPdfComprobante(id);
        String filename = "comprobante_devolucion_" + comprobante.numeroComprobante() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
