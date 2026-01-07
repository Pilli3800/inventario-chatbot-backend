package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioCreateRequest;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.*;
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

    @Transactional
    public void registrarMovimiento(MovimientoInventarioCreateRequest request) {
        createValidator.validate(request);

        Item item = itemRepository.findByCodigoItem(request.codigoItem())
                .orElseThrow(() -> new ValidationException(List.of("El item no existe")));

        String identUsuario = userService.getIdentUsuario();
        if (identUsuario == null) {
            throw new ValidationException(List.of("Usuario no autenticado"));
        }
        User usuario = userRepository.findByIdentUsuario(identUsuario)
                .orElseThrow(() ->
                        new ValidationException(List.of("Usuario autenticado no encontrado"))
                );

        validarPermisoPorTipoMovimiento(request.tipoMovimiento(), usuario);

        switch (request.tipoMovimiento()) {
            case ENTRADA -> procesarEntrada(request, item, usuario);
            case SALIDA -> procesarSalida(request, item, usuario);
            case TRANSFERENCIA -> procesarTransferencia(request, item, usuario);
            case DEVOLUCION -> procesarDevolucion(request, item, usuario);
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

        guardarMovimiento(TipoMovimiento.ENTRADA, request.cantidad(), usuario, null, destino, request.observaciones());
    }

    private void procesarSalida(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        Sede sedeOrigen = sedeRepository.findByCodigo(request.sedeOrigenCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede origen no existe")));

        InventarioSede origen = inventarioSedeService.obtenerExistente(item, sedeOrigen);

        origen.restarStock(request.cantidad());

        guardarMovimiento(TipoMovimiento.SALIDA, request.cantidad(), usuario, origen, null, request.observaciones());
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

        guardarMovimiento(TipoMovimiento.TRANSFERENCIA, request.cantidad(), usuario, origen, destino, request.observaciones());
    }

    private void procesarDevolucion(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        Sede sedeDestino = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede destino no existe")));

        InventarioSede destino = inventarioSedeService.obtenerOcrear(item, sedeDestino);

        destino.sumarStock(request.cantidad());

        guardarMovimiento(TipoMovimiento.DEVOLUCION, request.cantidad(), usuario, null, destino, request.observaciones());
    }

    private void guardarMovimiento(
            TipoMovimiento tipo,
            Long cantidad,
            User usuario,
            InventarioSede origen,
            InventarioSede destino,
            String observaciones
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setInventarioOrigen(origen);
        movimiento.setInventarioDestino(destino);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setObservaciones(observaciones);
    }

    private void validarPermisoPorTipoMovimiento(TipoMovimiento tipoMovimiento, User usuario) {
        boolean esLogistica = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombreRol().equals("LOGISTICA"));

        boolean esJefeCuadrilla = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombreRol().equals("JEFE_CUADRILLA"));

        boolean permitido = switch (tipoMovimiento) {
            case ENTRADA, TRANSFERENCIA -> esLogistica;
            case SALIDA, DEVOLUCION -> esLogistica || esJefeCuadrilla;
        };

        if (!permitido) {
            throw new ValidationException(
                    List.of("No tiene permisos para realizar este tipo de movimiento")
            );
        }
    }

}