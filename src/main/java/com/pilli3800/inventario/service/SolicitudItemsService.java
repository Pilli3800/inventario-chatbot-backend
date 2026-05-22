package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.solicituditems.*;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsDashboardDto;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsDashboardPorServicioDto;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsDto;
import com.pilli3800.inventario.data.dto.response.SolicitudItemsPendienteCierreDto;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.Servicio;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SolicitudItemsService {

    private static final LocalDateTime FECHA_MINIMA = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FECHA_MAXIMA = LocalDateTime.of(3000, 1, 1, 0, 0);

    private final SolicitudItemsRepository solicitudItemsRepository;
    private final SolicitudItemsCreateValidator createValidator;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CuadrillaRepository cuadrillaRepository;
    private final ServicioRepository servicioRepository;
    private final ItemRepository itemRepository;
    private final InventarioServicioRepository inventarioServicioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ValeSalidaService valeSalidaService;
    private final ComprobanteDevolucionService comprobanteDevolucionService;

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
                    request.servicioOrigenCodigo(),
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

    public SolicitudItemsDashboardDto getDashboard(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String servicioOrigenCodigo,
            String codigoCuadrilla,
            String identUsuario
    ) {
        Long solicitanteId = resolverSolicitanteDashboard(identUsuario);

        List<SolicitudItems> solicitudes = solicitudItemsRepository.buscarParaDashboard(
                fechaDesde != null ? fechaDesde.atStartOfDay() : FECHA_MINIMA,
                fechaHasta != null ? fechaHasta.atTime(23, 59, 59) : FECHA_MAXIMA,
                servicioOrigenCodigo != null && !servicioOrigenCodigo.isBlank()
                        ? TextNormalizer.normalizeCode(servicioOrigenCodigo)
                        : null,
                codigoCuadrilla != null && !codigoCuadrilla.isBlank()
                        ? codigoCuadrilla
                        : null,
                solicitanteId
        );

        EnumMap<EstadoSolicitudItems, Long> porEstado = inicializarContadorEstados();
        Map<String, EnumMap<EstadoSolicitudItems, Long>> estadosPorServicio = new TreeMap<>();
        List<SolicitudItemsPendienteCierreDto> pendientesCierre = new ArrayList<>();

        for (SolicitudItems solicitud : solicitudes) {
            EstadoSolicitudItems estado = solicitud.getEstado();
            porEstado.put(estado, porEstado.get(estado) + 1);

            String codigoServicio = solicitud.getServicioOrigen().getCodigo();
            EnumMap<EstadoSolicitudItems, Long> contadorServicio =
                    estadosPorServicio.computeIfAbsent(
                            codigoServicio,
                            key -> inicializarContadorEstados()
                    );
            contadorServicio.put(estado, contadorServicio.get(estado) + 1);

            if (estado == EstadoSolicitudItems.ENTREGADO) {
                pendientesCierre.add(new SolicitudItemsPendienteCierreDto(
                        solicitud.getId(),
                        solicitud.getCuadrilla().getCodigoCuadrilla(),
                        codigoServicio,
                        estado,
                        solicitud.getFechaEntrega()
                ));
            }
        }

        Long pendientes = porEstado.get(EstadoSolicitudItems.PENDIENTE);
        Long aprobadas = porEstado.get(EstadoSolicitudItems.APROBADA);
        Long rechazadas = porEstado.get(EstadoSolicitudItems.RECHAZADA);
        Long entregadas = porEstado.get(EstadoSolicitudItems.ENTREGADO);
        Long devueltas = porEstado.get(EstadoSolicitudItems.DEVUELTA);
        Long cerradasSinDevolucion = porEstado.get(EstadoSolicitudItems.CERRADA_SIN_DEVOLUCION);
        Long abiertas = pendientes + aprobadas + entregadas;
        Long finales = rechazadas + devueltas + cerradasSinDevolucion;

        List<SolicitudItemsDashboardPorServicioDto> porServicio = estadosPorServicio
                .entrySet()
                .stream()
                .map(entry -> toDashboardPorServicio(entry.getKey(), entry.getValue()))
                .toList();

        return new SolicitudItemsDashboardDto(
                (long) solicitudes.size(),
                abiertas,
                finales,
                pendientes,
                aprobadas,
                rechazadas,
                entregadas,
                devueltas,
                cerradasSinDevolucion,
                porEstado,
                porServicio,
                pendientesCierre
        );
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
                        EstadoSolicitudItems.APROBADA,
                        EstadoSolicitudItems.ENTREGADO
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

        Servicio servicioOrigen = servicioRepository
                .findByCodigo(TextNormalizer.normalizeCode(request.servicioOrigenCodigo()))
                .orElseThrow(() ->
                        new ValidationException(List.of("El servicio origen no existe"))
                );

        if (!servicioOrigen.isEnabled()) {
            throw new ValidationException(
                    List.of("El servicio origen esta desactivado")
            );
        }

        if (!cuadrilla.getServicio().getId().equals(servicioOrigen.getId())) {
            throw new ValidationException(
                    List.of("El servicio origen debe corresponder al servicio de la cuadrilla")
            );
        }

        SolicitudItems solicitud = new SolicitudItems();
        solicitud.setCuadrilla(cuadrilla);
        solicitud.setSolicitante(solicitante);
        solicitud.setServicioOrigen(servicioOrigen);
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
        Map<Long, InventarioServicio> inventariosPorItem = new LinkedHashMap<>();
        List<String> erroresStock = new ArrayList<>();
        Long servicioId = solicitud.getCuadrilla().getServicio().getId();

        List<SolicitudItemsDetalle> detallesOrdenados =
                solicitud.getDetalles()
                        .stream()
                        .sorted(Comparator.comparing(d -> d.getItem().getId()))
                        .toList();

        for (SolicitudItemsDetalle detalle : detallesOrdenados) {
            InventarioServicio inventario = inventarioServicioRepository
                    .findByServicioIdAndItemId(
                            servicioId,
                            detalle.getItem().getId()
                    )
                    .orElseThrow(() -> new ValidationException(
                            List.of("El item no esta asignado al inventario del servicio: "
                                    + detalle.getItem().getCodigoItem())
                    ));

            if (inventario.getStockActual() < detalle.getCantidad()) {
                erroresStock.add(
                        "Stock insuficiente para el item: "
                                + detalle.getItem().getCodigoItem()
                                + " (solicitado: " + detalle.getCantidad()
                                + ", stock: " + inventario.getStockActual() + ")"
                );
            }

            inventariosPorItem.put(detalle.getItem().getId(), inventario);
        }

        if (!erroresStock.isEmpty()) {
            throw new ValidationException(erroresStock);
        }

        for (SolicitudItemsDetalle detalle : detallesOrdenados) {
            InventarioServicio inventario =
                    inventariosPorItem.get(detalle.getItem().getId());
            inventario.restarStock(detalle.getCantidad());

            registrarSalida(
                    detalle,
                    inventario,
                    solicitud.getCuadrilla(),
                    usuarioAprobacion,
                    solicitud
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

        SolicitudItems solicitudEntregada = solicitudItemsRepository.save(solicitud);
        valeSalidaService.generarValeSiNoExiste(solicitudEntregada);
    }

    @Transactional
    public SolicitudItemsDto devolverSolicitud(
            Long id,
            SolicitudItemsDevolverRequest request
    ) {
        SolicitudItems solicitud = obtenerSolicitudPorId(id);

        if (solicitud.getEstado() != EstadoSolicitudItems.ENTREGADO) {
            throw new ValidationException(
                    List.of("Solo se puede devolver una solicitud entregada")
            );
        }

        User usuarioDevolucion = obtenerUsuarioAutenticado();
        validarDetalleDevolucion(request);

        Map<String, SolicitudItemsDetalle> detallesPorCodigo = new HashMap<>();
        for (SolicitudItemsDetalle detalle : solicitud.getDetalles()) {
            detallesPorCodigo.put(detalle.getItem().getCodigoItem(), detalle);
        }

        Set<String> codigosProcesados = new HashSet<>();
        Long servicioId = solicitud.getCuadrilla().getServicio().getId();

        for (SolicitudItemsDetalleRequest detalleRequest : request.detalles()) {
            String codigoItem = TextNormalizer.normalizeCode(detalleRequest.codigoItem());
            if (!codigosProcesados.add(codigoItem)) {
                throw new ValidationException(
                        List.of("No se permiten items duplicados en la devolucion")
                );
            }

            SolicitudItemsDetalle detalleSolicitud = detallesPorCodigo.get(codigoItem);
            if (detalleSolicitud == null) {
                throw new ValidationException(
                        List.of("El item no pertenece a la solicitud: " + codigoItem)
                );
            }

            Long cantidadYaDevuelta = movimientoInventarioRepository
                    .sumarCantidadDevueltaPorSolicitudEItem(
                            solicitud.getId(),
                            detalleSolicitud.getItem()
                    );
            Long cantidadDisponibleParaDevolver =
                    detalleSolicitud.getCantidad() - cantidadYaDevuelta;

            if (detalleRequest.cantidad() > cantidadDisponibleParaDevolver) {
                throw new ValidationException(
                        List.of("La cantidad a devolver supera lo pendiente para el item: "
                                + codigoItem
                                + " (pendiente: " + cantidadDisponibleParaDevolver + ")")
                );
            }

            InventarioServicio inventarioServicio = inventarioServicioRepository
                    .findByServicioIdAndItemId(
                            servicioId,
                            detalleSolicitud.getItem().getId()
                    )
                    .orElseThrow(() -> new ValidationException(
                            List.of("No existe inventario del servicio para el item: " + codigoItem)
                    ));

            inventarioServicio.sumarStock(detalleRequest.cantidad());
            registrarDevolucion(
                    detalleSolicitud,
                    inventarioServicio,
                    solicitud,
                    usuarioDevolucion,
                    detalleRequest.cantidad(),
                    request.observaciones()
            );
        }

        solicitud.setEstado(EstadoSolicitudItems.DEVUELTA);
        solicitud.setFechaDevolucion(LocalDateTime.now());
        solicitud.setUsuarioDevolucion(usuarioDevolucion);
        solicitud.setObservacionesDevolucion(request.observaciones());

        SolicitudItems solicitudDevuelta = solicitudItemsRepository.save(solicitud);
        comprobanteDevolucionService.generarComprobanteSiNoExiste(solicitudDevuelta);

        return SolicitudItemsDto.from(solicitudDevuelta);
    }

    @Transactional
    public SolicitudItemsDto cerrarSinDevolucion(
            Long id,
            SolicitudItemsCerrarSinDevolucionRequest request
    ) {
        SolicitudItems solicitud = obtenerSolicitudPorId(id);

        if (solicitud.getEstado() != EstadoSolicitudItems.ENTREGADO) {
            throw new ValidationException(
                    List.of("Solo se puede cerrar sin devolucion una solicitud entregada")
            );
        }

        User usuarioCierre = obtenerUsuarioAutenticado();

        solicitud.setEstado(EstadoSolicitudItems.CERRADA_SIN_DEVOLUCION);
        solicitud.setFechaCierre(LocalDateTime.now());
        solicitud.setUsuarioCierre(usuarioCierre);
        solicitud.setObservacionesCierre(request != null ? request.observaciones() : null);

        return SolicitudItemsDto.from(solicitudItemsRepository.save(solicitud));
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

    private Long resolverSolicitanteDashboard(String identUsuarioFiltro) {
        List<String> roles = userService.getUserRoles();
        boolean esJefeCuadrilla = roles.contains("ROLE_JEFE_CUADRILLA");

        if (esJefeCuadrilla) {
            return obtenerUsuarioAutenticado().getId();
        }

        if (identUsuarioFiltro == null || identUsuarioFiltro.isBlank()) {
            return null;
        }

        return userRepository
                .findByIdentUsuario(identUsuarioFiltro)
                .orElseThrow(() -> new ValidationException(
                        List.of("Usuario solicitante no encontrado")
                ))
                .getId();
    }

    private EnumMap<EstadoSolicitudItems, Long> inicializarContadorEstados() {
        EnumMap<EstadoSolicitudItems, Long> contador =
                new EnumMap<>(EstadoSolicitudItems.class);
        for (EstadoSolicitudItems estado : EstadoSolicitudItems.values()) {
            contador.put(estado, 0L);
        }
        return contador;
    }

    private SolicitudItemsDashboardPorServicioDto toDashboardPorServicio(
            String servicioOrigenCodigo,
            EnumMap<EstadoSolicitudItems, Long> porEstado
    ) {
        Long pendientes = porEstado.get(EstadoSolicitudItems.PENDIENTE);
        Long aprobadas = porEstado.get(EstadoSolicitudItems.APROBADA);
        Long entregadas = porEstado.get(EstadoSolicitudItems.ENTREGADO);
        Long devueltas = porEstado.get(EstadoSolicitudItems.DEVUELTA);
        Long cerradasSinDevolucion = porEstado.get(EstadoSolicitudItems.CERRADA_SIN_DEVOLUCION);
        Long rechazadas = porEstado.get(EstadoSolicitudItems.RECHAZADA);

        return new SolicitudItemsDashboardPorServicioDto(
                servicioOrigenCodigo,
                pendientes + aprobadas + entregadas + devueltas + cerradasSinDevolucion + rechazadas,
                pendientes,
                aprobadas,
                entregadas,
                devueltas,
                cerradasSinDevolucion,
                rechazadas
        );
    }

    private void validarDetalleDevolucion(SolicitudItemsDevolverRequest request) {
        List<String> errores = new ArrayList<>();

        if (request == null || request.detalles() == null || request.detalles().isEmpty()) {
            throw new ValidationException(
                    List.of("La devolucion debe tener al menos un item")
            );
        }

        for (SolicitudItemsDetalleRequest detalle : request.detalles()) {
            if (detalle == null) {
                errores.add("Los items de la devolucion no son validos");
                continue;
            }
            if (detalle.codigoItem() == null || detalle.codigoItem().isBlank()) {
                errores.add("El codigo de item es obligatorio");
            }
            if (detalle.cantidad() == null || detalle.cantidad() <= 0) {
                errores.add("La cantidad a devolver debe ser mayor a cero");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidationException(errores);
        }
    }

    private void registrarSalida(
            SolicitudItemsDetalle detalle,
            InventarioServicio inventarioServicioOrigen,
            Cuadrilla cuadrilla,
            User usuario,
            SolicitudItems solicitud
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA_CUADRILLA);
        movimiento.setCantidad(detalle.getCantidad());
        movimiento.setUsuario(usuario);
        movimiento.setInventarioServicioOrigen(inventarioServicioOrigen);
        movimiento.setInventarioDestino(null);
        movimiento.setInventarioOrigen(null);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setObservaciones("Salida por aprobacion de solicitud " + solicitud.getId());
        movimiento.setCuadrilla(cuadrilla);
        movimiento.setSolicitud(solicitud);

        movimientoInventarioRepository.save(movimiento);
    }

    private void registrarDevolucion(
            SolicitudItemsDetalle detalle,
            InventarioServicio inventarioServicioDestino,
            SolicitudItems solicitud,
            User usuario,
            Long cantidad,
            String observaciones
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(TipoMovimiento.DEVOLUCION);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setInventarioServicioDestino(inventarioServicioDestino);
        movimiento.setInventarioServicioOrigen(null);
        movimiento.setInventarioDestino(null);
        movimiento.setInventarioOrigen(null);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setObservaciones(observaciones != null && !observaciones.isBlank()
                ? observaciones
                : "Devolucion de solicitud " + solicitud.getId() + " item " + detalle.getItem().getCodigoItem());
        movimiento.setCuadrilla(solicitud.getCuadrilla());
        movimiento.setSolicitud(solicitud);

        movimientoInventarioRepository.save(movimiento);
    }
}
