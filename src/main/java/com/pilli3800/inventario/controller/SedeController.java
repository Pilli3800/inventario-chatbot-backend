package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.response.SedeDto;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {

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
}
