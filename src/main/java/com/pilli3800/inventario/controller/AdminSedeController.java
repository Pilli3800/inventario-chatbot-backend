package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.SedeUpdateRequest;
import com.pilli3800.inventario.data.dto.response.SedeDto;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sedes")
@PreAuthorize("hasRole('ADMINISTRACION')")
@RequiredArgsConstructor
public class AdminSedeController {

    private final SedeService sedeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<List<SedeDto>> getSedes() {
        return new SingleResponse<>(
                200,
                "/sedes",
                sedeService.getSedes()
        );
    }
    @GetMapping("/activas")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<List<SedeDto>> getSedesActivas() {
        return new SingleResponse<>(
                200,
                "/sedes/activas",
                sedeService.getSedesAtivas()
        );
    }

    @GetMapping("/{codigoSede}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<SedeDto> getSede(@PathVariable String codigoSede) {
        return new SingleResponse<>(
                200,
                "/sedes/{codigoSede}",
                sedeService.getSede(codigoSede)
        );
    }

    @PatchMapping("/{codigoSede}/activar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> enableSede(@PathVariable String codigoSede) {
        sedeService.enableSede(codigoSede);
        return new SingleResponse<>(200, "/api/admin/sedes/" + codigoSede + "/activar", "Sede activada");
    }

    @PatchMapping("/{codigoSede}/desactivar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> disableSede(@PathVariable String codigoSede) {
        sedeService.disableSede(codigoSede);
        return new SingleResponse<>(200, "/api/admin/sedes/" + codigoSede + "/desactivar", "Sede desactivada");
    }

    @PutMapping("/{codigoSede}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<SedeDto> updateSede(@Valid @RequestBody SedeUpdateRequest request, @PathVariable String codigoSede) {
        return new SingleResponse<>(
                200,
                "/sedes/{codigoSede}",
                sedeService.updateSede(request, codigoSede)
        );
    }
}
