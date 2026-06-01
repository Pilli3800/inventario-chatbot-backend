package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoCompraCreateRequest;
import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioCreateRequest;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.FacturaCompra;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.Proveedor;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.*;
import com.pilli3800.inventario.util.TextNormalizer;
import com.pilli3800.inventario.validator.MovimientoInventarioCreateValidator;
import com.pilli3800.inventario.validator.MovimientoCompraCreateValidator;
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
    private final MovimientoCompraCreateValidator compraCreateValidator;
    private final InventarioSedeService inventarioSedeService;
    private final InventarioServicioService inventarioServicioService;
    private final FacturaCompraService facturaCompraService;
    private final UserService userService;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CuadrillaRepository cuadrillaRepository;
    private final ServicioRepository servicioRepository;
    private final ProveedorRepository proveedorRepository;

    @Transactional
    public void registrarMovimiento(MovimientoInventarioCreateRequest request) {
        if (request.tipoMovimiento() == TipoMovimiento.DEVOLUCION) {
            throw new ValidationException(
                    List.of("La devolucion debe registrarse desde una solicitud entregada")
            );
        }

        createValidator.validate(request);

        if (request.tipoMovimiento() == TipoMovimiento.COMPRA) {
            compraCreateValidator.validate(
                    new MovimientoCompraCreateRequest(
                            request.codigoItem(),
                            request.sedeDestinoCodigo(),
                            request.codigoProveedor(),
                            request.numeroFactura(),
                            request.serieFactura(),
                            request.correlativoFactura(),
                            request.fechaEmisionFactura(),
                            request.cantidad(),
                            request.observaciones()
                    )
            );
        }

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
            case COMPRA -> procesarCompra(request, item, usuario);
            case SALIDA -> procesarSalida(request, item, usuario, cuadrilla);
            case SALIDA_CUADRILLA -> procesarSalidaCuadrilla(request, item, usuario, cuadrilla);
            case TRANSFERENCIA -> procesarTransferencia(request, item, usuario);
            case TRANSFERENCIA_SERVICIO -> procesarTransferenciaServicio(request, item, usuario);
            case RETORNO_A_SEDE -> procesarRetornoASede(request, item, usuario);
            case AJUSTE -> procesarAjuste(request, item, usuario);
            case DEVOLUCION -> throw new ValidationException(
                    List.of("La devolucion debe registrarse desde una solicitud entregada")
            );
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

        guardarMovimiento(
                TipoMovimiento.ENTRADA,
                request.cantidad(),
                usuario,
                null,
                destino,
                null,
                null,
                null,
                null,
                null,
                request.observaciones(),
                null
        );
    }

    private void procesarCompra(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        Sede sedeDestino = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede destino no existe")));

        String codigoProveedor = TextNormalizer.normalizeCode(request.codigoProveedor());
        Proveedor proveedor = proveedorRepository.findByCodigo(codigoProveedor)
                .orElseThrow(() -> new ValidationException(List.of("El proveedor no existe")));
        FacturaCompra facturaCompra = facturaCompraService.obtenerOCrearParaCompra(
                codigoProveedor,
                request.numeroFactura(),
                request.serieFactura(),
                request.correlativoFactura(),
                request.fechaEmisionFactura()
        );

        InventarioSede destino = inventarioSedeService.obtenerOcrear(item, sedeDestino);

        destino.sumarStock(request.cantidad());

        guardarMovimiento(
                TipoMovimiento.COMPRA,
                request.cantidad(),
                usuario,
                null,
                destino,
                null,
                null,
                proveedor,
                facturaCompra,
                facturaCompra.getNumeroFactura(),
                request.observaciones(),
                null
        );
    }

    private void procesarSalida(MovimientoInventarioCreateRequest request, Item item, User usuario, Cuadrilla cuadrilla) {
        Sede sedeOrigen = sedeRepository.findByCodigo(request.sedeOrigenCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede origen no existe")));

        InventarioSede origen = inventarioSedeService.obtenerExistente(item, sedeOrigen);

        origen.restarStock(request.cantidad());

        guardarMovimiento(
                TipoMovimiento.SALIDA,
                request.cantidad(),
                usuario,
                origen,
                null,
                null,
                null,
                null,
                null,
                null,
                request.observaciones(),
                cuadrilla
        );
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

        guardarMovimiento(
                TipoMovimiento.TRANSFERENCIA,
                request.cantidad(),
                usuario,
                origen,
                destino,
                null,
                null,
                null,
                null,
                null,
                request.observaciones(),
                null
        );
    }

    private void procesarTransferenciaServicio(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        Sede sedeOrigen = sedeRepository.findByCodigo(request.sedeOrigenCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede origen no existe")));

        Servicio servicioDestino = servicioRepository.findByCodigo(
                        TextNormalizer.normalizeCode(request.codigoServicio())
                )
                .orElseThrow(() -> new ValidationException(List.of("El servicio destino no existe")));

        InventarioSede origen = inventarioSedeService.obtenerExistente(item, sedeOrigen);
        InventarioServicio destino = inventarioServicioService.obtenerOcrear(item, servicioDestino);

        origen.restarStock(request.cantidad());
        destino.sumarStock(request.cantidad());

        guardarMovimiento(
                TipoMovimiento.TRANSFERENCIA_SERVICIO,
                request.cantidad(),
                usuario,
                origen,
                null,
                null,
                destino,
                null,
                null,
                null,
                request.observaciones(),
                null
        );
    }

    private void procesarSalidaCuadrilla(MovimientoInventarioCreateRequest request, Item item, User usuario, Cuadrilla cuadrilla) {
        // La salida a cuadrilla descuenta del inventario del servicio asociado a la cuadrilla
        Servicio servicio = cuadrilla.getServicio();

        InventarioServicio origen = inventarioServicioService.obtenerExistente(item, servicio);

        origen.restarStock(request.cantidad());

        guardarMovimiento(
                TipoMovimiento.SALIDA_CUADRILLA,
                request.cantidad(),
                usuario,
                null,
                null,
                origen,
                null,
                null,
                null,
                null,
                request.observaciones(),
                cuadrilla
        );
    }

    private void procesarRetornoASede(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        Servicio servicioOrigen = servicioRepository.findByCodigo(
                        TextNormalizer.normalizeCode(request.codigoServicio())
                )
                .orElseThrow(() -> new ValidationException(List.of("El servicio origen no existe")));

        Sede sedeDestino = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                .orElseThrow(() -> new ValidationException(List.of("La sede destino no existe")));

        InventarioServicio origen = inventarioServicioService.obtenerExistente(item, servicioOrigen);
        InventarioSede destino = inventarioSedeService.obtenerOcrear(item, sedeDestino);

        origen.restarStock(request.cantidad());
        destino.sumarStock(request.cantidad());

        guardarMovimiento(
                TipoMovimiento.RETORNO_A_SEDE,
                request.cantidad(),
                usuario,
                null,
                destino,
                origen,
                null,
                null,
                null,
                null,
                request.observaciones(),
                null
        );
    }

    private void procesarAjuste(MovimientoInventarioCreateRequest request, Item item, User usuario) {
        InventarioSede inventarioSede = null;
        InventarioServicio inventarioServicio = null;

        if (request.sedeDestinoCodigo() != null && !request.sedeDestinoCodigo().isBlank()) {
            Sede sede = sedeRepository.findByCodigo(request.sedeDestinoCodigo())
                    .orElseThrow(() -> new ValidationException(List.of("La sede del ajuste no existe")));
            inventarioSede = request.cantidad() > 0
                    ? inventarioSedeService.obtenerOcrear(item, sede)
                    : inventarioSedeService.obtenerExistente(item, sede);
        } else {
            Servicio servicio = servicioRepository.findByCodigo(
                            TextNormalizer.normalizeCode(request.codigoServicio())
                    )
                    .orElseThrow(() -> new ValidationException(List.of("El servicio del ajuste no existe")));
            inventarioServicio = request.cantidad() > 0
                    ? inventarioServicioService.obtenerOcrear(item, servicio)
                    : inventarioServicioService.obtenerExistente(item, servicio);
        }

        Long cantidadAbsoluta = Math.abs(request.cantidad());
        Long nuevoStock;
        if (inventarioSede != null) {
            if (request.cantidad() > 0) {
                inventarioSede.sumarStock(cantidadAbsoluta);
            } else {
                inventarioSede.restarStock(cantidadAbsoluta);
            }
            nuevoStock = inventarioSede.getStock();
        } else if (request.cantidad() > 0) {
            inventarioServicio.sumarStock(cantidadAbsoluta);
            nuevoStock = inventarioServicio.getStockActual();
        } else {
            inventarioServicio.restarStock(cantidadAbsoluta);
            nuevoStock = inventarioServicio.getStockActual();
        }

        guardarMovimiento(
                TipoMovimiento.AJUSTE,
                request.cantidad(),
                usuario,
                null,
                inventarioSede,
                null,
                inventarioServicio,
                null,
                null,
                null,
                observacionAjuste(nuevoStock, request.observaciones()),
                null
        );
    }

    private String observacionAjuste(Long nuevoStock, String observaciones) {
        return "El nuevo stock es: " + nuevoStock + ". " + observaciones;
    }

    private void guardarMovimiento(
            TipoMovimiento tipo,
            Long cantidad,
            User usuario,
            InventarioSede origen,
            InventarioSede destino,
            InventarioServicio servicioOrigen,
            InventarioServicio servicioDestino,
            Proveedor proveedor,
            FacturaCompra facturaCompra,
            String numeroFactura,
            String observaciones,
            Cuadrilla cuadrilla
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuario(usuario);
        movimiento.setInventarioOrigen(origen);
        movimiento.setInventarioDestino(destino);
        movimiento.setInventarioServicioOrigen(servicioOrigen);
        movimiento.setInventarioServicioDestino(servicioDestino);
        movimiento.setProveedor(proveedor);
        movimiento.setFacturaCompra(facturaCompra);
        movimiento.setNumeroFactura(numeroFactura);
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
            case ENTRADA, COMPRA, TRANSFERENCIA, TRANSFERENCIA_SERVICIO, RETORNO_A_SEDE, AJUSTE -> esLogistica;
            case SALIDA, SALIDA_CUADRILLA -> esJefeCuadrilla;
            case DEVOLUCION -> false;
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
                && request.tipoMovimiento() != TipoMovimiento.SALIDA_CUADRILLA) {
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
}


