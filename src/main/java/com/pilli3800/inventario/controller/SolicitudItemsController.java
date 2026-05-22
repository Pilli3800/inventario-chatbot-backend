package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.solicituditems.*;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsDashboardDto;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.service.SolicitudItemsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/solicitud-items")
@RequiredArgsConstructor
public class SolicitudItemsController {

    private final SolicitudItemsService solicitudItemsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SolicitudItemsDto> getSolicitudes(
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) String identUsuario,
            @RequestParam(required = false) String servicioOrigenCodigo,
            @RequestParam(required = false) EstadoSolicitudItems estado,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        Pageable pageableAjustado = ajustarOrden(pageable);

        SolicitudItemsSearchRequest request = new SolicitudItemsSearchRequest(
                codigoCuadrilla,
                identUsuario,
                servicioOrigenCodigo,
                estado,
                fechaDesde,
                fechaHasta,
                null
        );

        Page<SolicitudItemsDto> page =
                solicitudItemsService.getSolicitudes(request, pageableAjustado);

        return PageResponse.from(page);
    }

    @GetMapping("/dashboard")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<SolicitudItemsDashboardDto> getDashboard(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String servicioOrigenCodigo,
            @RequestParam(required = false) String codigoCuadrilla,
            @RequestParam(required = false) String identUsuario,
            @RequestParam(defaultValue = "false") boolean incluirRankingCuadrillas
    ) {
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/dashboard",
                solicitudItemsService.getDashboard(
                        fechaDesde,
                        fechaHasta,
                        servicioOrigenCodigo,
                        codigoCuadrilla,
                        identUsuario,
                        incluirRankingCuadrillas
                )
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<SolicitudItemsDto> getSolicitud(
            @PathVariable Long id
    ) {
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/" + id,
                solicitudItemsService.getSolicitud(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('JEFE_CUADRILLA')")
    public SingleResponse<SolicitudItemsDto> crearSolicitud(
            @Valid @RequestBody SolicitudItemsCreateRequest request
    ) {
        return new SingleResponse<>(
                201,
                "/api/solicitud-items",
                solicitudItemsService.crearSolicitud(request)
        );
    }

    @PatchMapping("/{id}/aprobar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<String> aprobarSolicitud(
            @PathVariable Long id,
            @RequestBody SolicitudItemsAprobarRequest request
    ) {
        solicitudItemsService.aprobarSolicitud(id, request);
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/" + id + "/aprobar",
                "Solicitud aprobada"
        );
    }

    @PatchMapping("/{id}/rechazar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<String> rechazarSolicitud(
            @PathVariable Long id,
            @RequestBody SolicitudItemsRechazarRequest request
    ) {
        solicitudItemsService.rechazarSolicitud(id, request);
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/" + id + "/rechazar",
                "Solicitud rechazada"
        );
    }

    @PatchMapping("/{id}/entregar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<String> entregarSolicitud(
            @PathVariable Long id,
            @RequestBody SolicitudItemsEntregarRequest request
    ) {
        solicitudItemsService.entregarSolicitud(id, request);
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/" + id + "/entregar",
                "Solicitud entregada"
        );
    }

    @PatchMapping("/{id}/devolver")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<SolicitudItemsDto> devolverSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudItemsDevolverRequest request
    ) {
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/" + id + "/devolver",
                solicitudItemsService.devolverSolicitud(id, request)
        );
    }

    @PatchMapping("/{id}/cerrar-sin-devolucion")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('LOGISTICA')")
    public SingleResponse<SolicitudItemsDto> cerrarSinDevolucion(
            @PathVariable Long id,
            @RequestBody SolicitudItemsCerrarSinDevolucionRequest request
    ) {
        return new SingleResponse<>(
                200,
                "/api/solicitud-items/" + id + "/cerrar-sin-devolucion",
                solicitudItemsService.cerrarSinDevolucion(id, request)
        );
    }

    private Pageable ajustarOrden(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if ("fechaSolicitud".equalsIgnoreCase(property)) {
                property = "fcCreacion";
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
