package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaCreateRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaSearchRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaUpdateRequest;
import com.pilli3800.inventario.data.dto.response.CuadrillaDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
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
@RequestMapping("/api/cuadrillas")
@RequiredArgsConstructor
public class CuadrillaController {

    private final CuadrillaService cuadrillaService;

    @GetMapping("/{codigoCuadrilla}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<CuadrillaDto> getCuadrilla(
            @PathVariable String codigoCuadrilla) {
        return new SingleResponse<>(
                200,
                "/api/logistica/cuadrillas/{codigoCuadrilla}",
                cuadrillaService.getCuadrilla(codigoCuadrilla)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<CuadrillaDto> getCuadrillas(
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) String identUsuarioJefe,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(page = 0, size = 5, sort = "codigoCuadrilla") Pageable pageable
    ) {

        CuadrillaSearchRequest request =
                new CuadrillaSearchRequest(
                        codigoCuadrilla,
                        identUsuarioJefe,
                        enabled
                );

        Page<CuadrillaDto> page =
                cuadrillaService.getCuadrillas(request, pageable);

        return PageResponse.from(page);
    }

}