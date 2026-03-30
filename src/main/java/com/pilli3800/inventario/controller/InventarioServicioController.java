package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.InventarioServicioCreateRequest;
import com.pilli3800.inventario.data.dto.request.InventarioServicioSearchRequest;
import com.pilli3800.inventario.data.dto.response.InventarioServicioDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.InventarioServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/logistica/inventarios-servicio")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated() and !hasRole('JEFE_CUADRILLA')")
public class InventarioServicioController {

    private final InventarioServicioService inventarioServicioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<InventarioServicioDto> getInventarioServicio(
            @RequestParam String codigoServicio,
            @RequestParam(required = false) String nombreItem,
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) TipoItem tipoItem,
            @RequestParam(required = false) Boolean enabledItem,
            @RequestParam(required = false) Boolean conStock,
            @PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable
    ) {
        Pageable pageableAjustado = ajustarOrden(pageable);

        InventarioServicioSearchRequest request =
                new InventarioServicioSearchRequest(
                        codigoServicio,
                        nombreItem,
                        codigoItem,
                        tipoItem,
                        enabledItem,
                        conStock
                );

        Page<InventarioServicioDto> page =
                inventarioServicioService.getInventario(request, pageableAjustado);

        return PageResponse.from(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<String> asignarItemAServicio(
            @Valid @RequestBody InventarioServicioCreateRequest request
    ) {
        inventarioServicioService.asignarItemAServicio(request);
        return new SingleResponse<>(
                201,
                "/api/logistica/inventarios-servicio",
                "Item asignado al servicio correctamente"
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> eliminarAsignacion(@PathVariable Long id) {
        inventarioServicioService.eliminarAsignacion(id);
        return new SingleResponse<>(
                200,
                "/api/logistica/inventarios-servicio/" + id,
                "Asignacion eliminada correctamente"
        );
    }

    private Pageable ajustarOrden(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();

            if ("nombreItem".equalsIgnoreCase(property)) {
                property = "item.nombre";
            } else if ("codigoItem".equalsIgnoreCase(property)) {
                property = "item.codigoItem";
            } else if ("tipoItem".equalsIgnoreCase(property)) {
                property = "item.tipo";
            } else if ("enabledItem".equalsIgnoreCase(property)) {
                property = "item.enabled";
            } else if ("codigoServicio".equalsIgnoreCase(property)) {
                property = "servicio.codigo";
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
