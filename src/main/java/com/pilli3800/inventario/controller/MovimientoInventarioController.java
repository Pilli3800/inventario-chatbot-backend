package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioCreateRequest;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICA', 'JEFE_CUADRILLA')")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<String> registrarMovimiento(
            @Valid @RequestBody MovimientoInventarioCreateRequest request
    ) {

        movimientoInventarioService.registrarMovimiento(request);

        return new SingleResponse<>(
                201,
                "/api/movimientos",
                "Movimiento de inventario registrado correctamente"
        );
    }
}