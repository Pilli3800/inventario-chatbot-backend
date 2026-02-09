package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioCreateRequest;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.*;
import com.pilli3800.inventario.util.TextNormalizer;
import com.pilli3800.inventario.validator.MovimientoInventarioCreateValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoInventarioService {

    private final ItemRepository itemRepository;
    private final SedeRepository sedeRepository;
    private final UserRepository userRepository;
    private final MovimientoInventarioCreateValidator createValidator;
    private final InventarioSedeService inventarioSedeService;
    private final UserService userService;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CuadrillaRepository cuadrillaRepository;

    @Transactional
    public void registrarMovimiento(MovimientoInventarioCreateRequest request) {
        createValidator.validate(request);

        String codigoItem = TextNormalizer.normalizeCode(request.codigoItem());
        Item item = itemRepository.findByCodigoItem(codigoItem)
                .orElseThrow(() -> new ValidationException(List.of("El item no existe")));

        String identUsuario = userService.getIdentUsuario();
        if (identUsuario == null) {
            throw new ValidationException(List.of("Usuario no autenticado"));
        }
        User usuario = userRepository.findByIdentUsuario(identUsuario)
                .orElseThrow(() ->
                        new ValidationException(List.of("Usuario autenticado no encontrado"))
                );

        Cuadrilla cuadrilla = resolverCuadrillaSiAplica(request, usuario);

        validarPermisoPorTipoMovimiento(request.tipoMovimiento(), usuario);

        switch (request.tipoMovimiento()) {
            case ENTRADA -> procesarEntrada(request, item, usuario);
            case SALIDA -> procesarSalida(request, item, usuario, cuadrilla);
            case TRANSFERENCIA -> procesarTransferencia(request, item, usuario);
            case DEVOLUCION -> procesarDevolucion(request, item, usuario, cuadrilla);
            default -> throw new ValidationException(
                    List.of("Tipo de movimiento no soportado")
            );
        }
    }

    private void procesarEntrada(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        Sede sedeDestino = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede destino no existe")));

        InventarioSede destino = inventarioSedeService.obtenerOcrear(item, sedeDestino);

        destino.sumarStock(request.cantidad());

        guardarMovimiento(TipoMovimiento.ENTRADA, request.cantidad(), usuario, null, destino, request.observaciones(), null);
    }

    private void procesarSalida(MovimientoInventarioCreateRequest request, Item item, User usuario, Cuadrilla cuadrilla) {
        Sede sedeOrigen = sedeRepository.findByCodigo(request.sedeOrigenCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede origen no existe")));

        InventarioSede origen = inventarioSedeService.obtenerExistente(item, sedeOrigen);

        origen.restarStock(request.cantidad());

        guardarMovimiento(TipoMovimiento.SALIDA, request.cantidad(), usuario, origen, null, request.observaciones(), cuadrilla);
    }

    private void procesarTransferencia(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        if (request.sedeOrigenCodigo().equals(request.sedeDestinoCodigo())) {
            throw new ValidationException(List.of("La sede origen y destino no pueden ser iguales"));
        }

        Sede sedeOrigen = sedeRepository.findByCodigo(request.sedeOrigenCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede origen no existe")));

        Sede sedeDestino = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede destino no existe")));

        InventarioSede primero;
        InventarioSede segundo;

        if (sedeOrigen.getId() < sedeDestino.getId()) {
            primero = inventarioSedeService.obtenerExistente(item, sedeOrigen);
            segundo = inventarioSedeService.obtenerOcrear(item, sedeDestino);
        } else {
            primero = inventarioSedeService.obtenerOcrear(item, sedeDestino);
            segundo = inventarioSedeService.obtenerExistente(item, sedeOrigen);
        }

        InventarioSede origen = (sedeOrigen.getId() < sedeDestino.getId()) ? primero : segundo;
        InventarioSede destino = (sedeOrigen.getId() < sedeDestino.getId()) ? segundo : primero;

        origen.restarStock(request.cantidad());
        destino.sumarStock(request.cantidad());

        guardarMovimiento(TipoMovimiento.TRANSFERENCIA, request.cantidad(), usuario, origen, destino, request.observaciones(), null);
    }

    private void procesarDevolucion(MovimientoInventarioCreateRequest request, Item item, User usuario, Cuadrilla cuadrilla) {
        validarDevolucion(item, cuadrilla, usuario, request.cantidad(), request.sedeDestinoCodigo());

        Sede sedeDestino = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede destino no existe")));

        InventarioSede destino = inventarioSedeService.obtenerOcrear(item, sedeDestino);

        destino.sumarStock(request.cantidad());

        guardarMovimiento(TipoMovimiento.DEVOLUCION, request.cantidad(), usuario, null, destino, request.observaciones(), cuadrilla);
    }

    private void guardarMovimiento(
            TipoMovimiento tipo,
            Long cantidad,
            User usuario,
            InventarioSede origen,
            InventarioSede destino,
            String observaciones,
            Cuadrilla cuadrilla
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setInventarioOrigen(origen);
        movimiento.setInventarioDestino(destino);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setObservaciones(observaciones);
        movimiento.setCuadrilla(cuadrilla);

        movimientoInventarioRepository.save(movimiento);
    }

    private void validarPermisoPorTipoMovimiento(TipoMovimiento tipoMovimiento, User usuario) {
        boolean esLogistica = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombreRol().equals("LOGISTICA"));

        boolean esJefeCuadrilla = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombreRol().equals("JEFE_CUADRILLA"));

        boolean permitido = switch (tipoMovimiento) {
            case ENTRADA, TRANSFERENCIA -> esLogistica;
            case SALIDA, DEVOLUCION -> esJefeCuadrilla;
        };

        if (!permitido) {
            throw new ValidationException(
                    List.of("No tiene permisos para realizar este tipo de movimiento")
            );
        }
    }
    private Cuadrilla resolverCuadrillaSiAplica(
            MovimientoInventarioCreateRequest request,
            User usuario
    ) {

        if (request.tipoMovimiento() != TipoMovimiento.SALIDA
                && request.tipoMovimiento() != TipoMovimiento.DEVOLUCION) {
            return null;
        }


        Cuadrilla cuadrilla = cuadrillaRepository
                .findByCodigoCuadrilla(request.codigoCuadrilla())
                .orElseThrow(() ->
                        new ValidationException(
                                List.of("La cuadrilla no existe")
                        )
                );

        if (!cuadrilla.isEnabled()) {
            throw new ValidationException(
                    List.of("La cuadrilla está desactivada")
            );
        }

        if (!cuadrilla.getJefeCuadrilla().getId().equals(usuario.getId())) {
            throw new ValidationException(
                    List.of("Solo el jefe de la cuadrilla puede registrar salidas para ella")
            );
        }

        return cuadrilla;
    }

    private void validarDevolucion(
            Item item,
            Cuadrilla cuadrilla,
            User usuario,
            Long cantidadDevolucion,
            String sedeDestinoCodigo
    ) {

        List<MovimientoInventario> movimientos =
                movimientoInventarioRepository
                        .findMovimientosPorUsuarioCuadrillaItemOrdenados(
                                usuario,
                                cuadrilla,
                                item
                        );

        if (movimientos.isEmpty()) {
            throw new ValidationException(
                    List.of("No existe ningún movimiento previo para este item y cuadrilla")
            );
        }

        MovimientoInventario ultimoMovimiento = movimientos.getFirst();

        // El último movimiento DEBE ser SALIDA
        if (ultimoMovimiento.getTipoMovimiento() != TipoMovimiento.SALIDA) {
            throw new ValidationException(
                    List.of(
                            "No se puede registrar una devolución porque el último movimiento no es una salida"
                    )
            );
        }

        // Validar sede de devolución (coherencia espacial)
        Sede sedeSalida = ultimoMovimiento
                .getInventarioOrigen()
                .getSede();

        if (!sedeSalida.getCodigo().equals(sedeDestinoCodigo)) {
            throw new ValidationException(
                    List.of(
                            "La devolución debe realizarse a la misma sede desde donde se realizó la salida"
                    )
            );
        }

        // Cantidad > 0
        if (cantidadDevolucion <= 0) {
            throw new ValidationException(
                    List.of("La cantidad a devolver debe ser mayor a cero")
            );
        }

        // Cantidad <= última salida
        if (cantidadDevolucion > ultimoMovimiento.getCantidad()) {
            throw new ValidationException(
                    List.of(
                            "La cantidad a devolver no puede ser mayor a la cantidad de la última salida"
                    )
            );
        }
    }


}
