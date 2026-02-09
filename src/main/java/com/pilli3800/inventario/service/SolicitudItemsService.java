package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.solicituditems.*;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsDto;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItemsDetalle;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.*;
import com.pilli3800.inventario.specifications.SolicitudItemsSpecifications;
import com.pilli3800.inventario.util.TextNormalizer;
import com.pilli3800.inventario.validator.SolicitudItemsCreateValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SolicitudItemsService {

    private final SolicitudItemsRepository solicitudItemsRepository;
    private final SolicitudItemsCreateValidator createValidator;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CuadrillaRepository cuadrillaRepository;
    private final SedeRepository sedeRepository;
    private final ItemRepository itemRepository;
    private final InventarioSedeRepository inventarioSedeRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public Page<SolicitudItemsDto> getSolicitudes(
            SolicitudItemsSearchRequest request,
            Pageable pageable
    ) {
        SolicitudItemsSearchRequest requestAjustado = request;
        if (request.identUsuario() != null && !request.identUsuario().isBlank()) {
            User solicitante = userRepository
                    .findByIdentUsuario(request.identUsuario())
                    .orElseThrow(() -> new ValidationException(
                            List.of("Usuario solicitante no encontrado")
                    ));

            requestAjustado = new SolicitudItemsSearchRequest(
                    request.codigoCuadrilla(),
                    request.identUsuario(),
                    request.sedeOrigenCodigo(),
                    request.estado(),
                    request.fechaDesde(),
                    request.fechaHasta(),
                    solicitante.getId()
            );
        }

        Specification<SolicitudItems> spec =
                SolicitudItemsSpecifications.search(requestAjustado);

        return solicitudItemsRepository
                .findAll(spec, pageable)
                .map(SolicitudItemsDto::from);
    }

    public SolicitudItemsDto getSolicitud(Long id) {
        return SolicitudItemsDto.from(obtenerSolicitudPorId(id));
    }

    @Transactional
    public SolicitudItemsDto crearSolicitud(
            SolicitudItemsCreateRequest request
    ) {
        createValidator.validate(request);

        User solicitante = obtenerUsuarioAutenticado();

        if (solicitudItemsRepository.existsBySolicitanteAndEstadoIn(
                solicitante,
                List.of(
                        EstadoSolicitudItems.PENDIENTE,
                        EstadoSolicitudItems.APROBADA
                ))
        ) {
            throw new ValidationException(
                    List.of("Ya tiene una solicitud activa")
            );
        }

        Cuadrilla cuadrilla = cuadrillaRepository
                .findByCodigoCuadrilla(request.codigoCuadrilla())
                .orElseThrow(() ->
                        new ValidationException(List.of("La cuadrilla no existe"))
                );

        if (!cuadrilla.isEnabled()) {
            throw new ValidationException(
                    List.of("La cuadrilla esta desactivada")
            );
        }

        if (!cuadrilla.getJefeCuadrilla().getId().equals(solicitante.getId())) {
            throw new ValidationException(
                    List.of("Solo el jefe de la cuadrilla puede solicitar items")
            );
        }

        Sede sedeOrigen = sedeRepository
                .findByCodigo(request.sedeOrigenCodigo())
                .orElseThrow(() ->
                        new ValidationException(List.of("La sede origen no existe"))
                );

        SolicitudItems solicitud = new SolicitudItems();
        solicitud.setCuadrilla(cuadrilla);
        solicitud.setSolicitante(solicitante);
        solicitud.setSedeOrigen(sedeOrigen);
        solicitud.setEstado(EstadoSolicitudItems.PENDIENTE);
        solicitud.setObservaciones(request.observaciones());

        for (SolicitudItemsDetalleRequest itemRequest : request.detalles()) {
            String codigoItem = TextNormalizer.normalizeCode(itemRequest.codigoItem());
            Item item = itemRepository
                    .findByCodigoItem(codigoItem)
                    .orElseThrow(() -> new ValidationException(
                            List.of("El item no existe: " + codigoItem)
                    ));

            SolicitudItemsDetalle detalle = new SolicitudItemsDetalle();
            detalle.setSolicitud(solicitud);
            detalle.setItem(item);
            detalle.setCantidad(itemRequest.cantidad());
            solicitud.getDetalles().add(detalle);
        }

        return SolicitudItemsDto.from(
                solicitudItemsRepository.save(solicitud)
        );
    }

    @Transactional
    public void aprobarSolicitud(
            Long id,
            SolicitudItemsAprobarRequest request
    ) {
        SolicitudItems solicitud = obtenerSolicitudPorId(id);

        if (solicitud.getEstado() != EstadoSolicitudItems.PENDIENTE) {
            throw new ValidationException(
                    List.of("La solicitud no esta pendiente de aprobacion")
            );
        }

        User usuarioAprobacion = obtenerUsuarioAutenticado();
        Map<Long, InventarioSede> inventariosPorItem = new LinkedHashMap<>();
        List<String> erroresStock = new ArrayList<>();

        List<SolicitudItemsDetalle> detallesOrdenados =
                solicitud.getDetalles()
                        .stream()
                        .sorted(Comparator.comparing(d -> d.getItem().getId()))
                        .toList();

        for (SolicitudItemsDetalle detalle : detallesOrdenados) {
            InventarioSede inventario = inventarioSedeRepository
                    .findByItemIdAndSedeId(
                            detalle.getItem().getId(),
                            solicitud.getSedeOrigen().getId()
                    )
                    .orElseThrow(() -> new ValidationException(
                            List.of("El item no esta asignado a la sede origen: "
                                    + detalle.getItem().getCodigoItem())
                    ));

            if (inventario.getStock() < detalle.getCantidad()) {
                erroresStock.add(
                        "Stock insuficiente para el item: "
                                + detalle.getItem().getCodigoItem()
                                + " (solicitado: " + detalle.getCantidad()
                                + ", stock: " + inventario.getStock() + ")"
                );
            }

            inventariosPorItem.put(detalle.getItem().getId(), inventario);
        }

        if (!erroresStock.isEmpty()) {
            throw new ValidationException(erroresStock);
        }

        for (SolicitudItemsDetalle detalle : detallesOrdenados) {
            InventarioSede inventario =
                    inventariosPorItem.get(detalle.getItem().getId());
            inventario.restarStock(detalle.getCantidad());

            registrarSalida(
                    detalle,
                    inventario,
                    solicitud.getCuadrilla(),
                    usuarioAprobacion,
                    solicitud.getId()
            );
        }

        solicitud.setEstado(EstadoSolicitudItems.APROBADA);
        solicitud.setObservacionesAprobacion(request != null ? request.observaciones() : null);
        solicitud.setFechaAprobacion(LocalDateTime.now());
        solicitud.setUsuarioAprobacion(usuarioAprobacion);

        solicitudItemsRepository.save(solicitud);
    }

    @Transactional
    public void rechazarSolicitud(
            Long id,
            SolicitudItemsRechazarRequest request
    ) {
        SolicitudItems solicitud = obtenerSolicitudPorId(id);

        if (solicitud.getEstado() != EstadoSolicitudItems.PENDIENTE) {
            throw new ValidationException(
                    List.of("La solicitud no esta pendiente de aprobacion")
            );
        }

        User usuarioAprobacion = obtenerUsuarioAutenticado();

        solicitud.setEstado(EstadoSolicitudItems.RECHAZADA);
        solicitud.setObservacionesAprobacion(request != null ? request.observaciones() : null);
        solicitud.setFechaAprobacion(LocalDateTime.now());
        solicitud.setUsuarioRechazo(usuarioAprobacion);

        solicitudItemsRepository.save(solicitud);
    }

    @Transactional
    public void entregarSolicitud(
            Long id,
            SolicitudItemsEntregarRequest request
    ) {
        SolicitudItems solicitud = obtenerSolicitudPorId(id);

        if (solicitud.getEstado() != EstadoSolicitudItems.APROBADA) {
            throw new ValidationException(
                    List.of("La solicitud no esta aprobada")
            );
        }

        solicitud.setEstado(EstadoSolicitudItems.ENTREGADO);
        solicitud.setObservacionesEntrega(request != null ? request.observaciones() : null);
        solicitud.setFechaEntrega(LocalDateTime.now());
        solicitud.setUsuarioEntrega(obtenerUsuarioAutenticado());

        solicitudItemsRepository.save(solicitud);
    }

    private SolicitudItems obtenerSolicitudPorId(Long id) {
        return solicitudItemsRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException(
                        List.of("Solicitud no encontrada")
                ));
    }

    private User obtenerUsuarioAutenticado() {
        String identUsuario = userService.getIdentUsuario();
        if (identUsuario == null) {
            throw new ValidationException(List.of("Usuario no autenticado"));
        }
        return userRepository
                .findByIdentUsuario(identUsuario)
                .orElseThrow(() -> new ValidationException(
                        List.of("Usuario autenticado no encontrado")
                ));
    }

    private void registrarSalida(
            SolicitudItemsDetalle detalle,
            InventarioSede inventarioOrigen,
            Cuadrilla cuadrilla,
            User usuario,
            Long idSolicitud
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
        movimiento.setCantidad(detalle.getCantidad());
        movimiento.setUsuario(usuario);
        movimiento.setInventarioOrigen(inventarioOrigen);
        movimiento.setInventarioDestino(null);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setObservaciones("Salida por aprobacion de solicitud " + idSolicitud);
        movimiento.setCuadrilla(cuadrilla);

        movimientoInventarioRepository.save(movimiento);
    }
}
