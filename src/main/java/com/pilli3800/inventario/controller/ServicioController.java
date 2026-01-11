package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.servicios.ServicioSearchRequest;
import com.pilli3800.inventario.data.dto.response.ServicioDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping("/{codigoServicio}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ServicioDto> getServicio(@PathVariable String codigoServicio) {
        return new SingleResponse<>(
                200,
                "/api/servicios/" + codigoServicio,
                servicioService.getServicio(codigoServicio)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ServicioDto> getServicios(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(page = 0, size = 5, sort = "nombre") Pageable pageable
    ) {

        ServicioSearchRequest request = new ServicioSearchRequest(codigo, nombre, enabled);

        return PageResponse.from(
                servicioService.getServicios(request, pageable)
        );
    }

}