package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.FacturaCompraCreateRequest;
import com.pilli3800.inventario.data.dto.request.FacturaCompraSearchRequest;
import com.pilli3800.inventario.data.dto.response.FacturaCompraDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.FacturaCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas-compra")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated() and !hasRole('JEFE_CUADRILLA')")
public class FacturaCompraController {

    private final FacturaCompraService facturaCompraService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<FacturaCompraDto> getFacturas(
            @RequestParam(required = false) String codigoProveedor,
            @RequestParam(required = false) String numeroFactura,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        FacturaCompraSearchRequest request = new FacturaCompraSearchRequest(
                codigoProveedor,
                numeroFactura
        );

        return PageResponse.from(
                facturaCompraService.getFacturas(request, pageable)
        );
    }

    @GetMapping("/{codigoProveedor}/{numeroFactura}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<FacturaCompraDto> getFactura(
            @PathVariable String codigoProveedor,
            @PathVariable String numeroFactura
    ) {
        return new SingleResponse<>(
                200,
                "/api/facturas-compra/" + codigoProveedor + "/" + numeroFactura,
                facturaCompraService.getFactura(codigoProveedor, numeroFactura)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<FacturaCompraDto> createFactura(
            @Valid @RequestBody FacturaCompraCreateRequest request
    ) {
        return new SingleResponse<>(
                201,
                "/api/facturas-compra",
                facturaCompraService.createFactura(request)
        );
    }
}
