package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioCreateRequest;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovimientoInventarioCreateValidator {

    private final ItemRepository itemRepository;

    public void validate(MovimientoInventarioCreateRequest request) {

        List<String> errors = new ArrayList<>();

        // Tipo de movimiento
        if (request.tipoMovimiento() == null) {
            errors.add("El tipo de movimiento es obligatorio");
        }

        if (request.cantidad() == null) {
            errors.add("La cantidad es obligatoria");
        } else if (request.tipoMovimiento() == TipoMovimiento.AJUSTE) {
            if (request.cantidad() == 0) {
                errors.add("La cantidad del ajuste debe ser distinta de cero");
            }
        } else if (request.cantidad() <= 0) {
            errors.add("La cantidad debe ser mayor a cero");
        }

        // Item existe y activo
        String codigoItem = TextNormalizer.normalizeCode(request.codigoItem());
        Item item = itemRepository
                .findByCodigoItem(codigoItem)
                .orElse(null);

        if (item == null) {
            errors.add("El item no existe");
        } else if (!item.isEnabled()) {
            errors.add("El item está desactivado");
        }

        // Usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            errors.add("Usuario no autenticado");
        }

        if (request.tipoMovimiento() == TipoMovimiento.ENTRADA) {
            if (request.sedeDestinoCodigo() == null || request.sedeDestinoCodigo().isBlank()) {
                errors.add("La entrada debe indicar sede destino");
            }
        }

        if (request.tipoMovimiento() == TipoMovimiento.SALIDA
                || request.tipoMovimiento() == TipoMovimiento.SALIDA_CUADRILLA) {

            if (request.codigoCuadrilla() == null || request.codigoCuadrilla().isBlank()) {
                errors.add("La salida debe estar asociada a una cuadrilla");
            }
        }

        if (request.tipoMovimiento() == TipoMovimiento.SALIDA) {
            if (request.sedeOrigenCodigo() == null || request.sedeOrigenCodigo().isBlank()) {
                errors.add("La salida debe indicar sede origen");
            }
        }

        if (request.tipoMovimiento() == TipoMovimiento.TRANSFERENCIA) {
            boolean tieneSedeDestino = request.sedeDestinoCodigo() != null && !request.sedeDestinoCodigo().isBlank();
            boolean tieneSedeOrigen = request.sedeOrigenCodigo() != null && !request.sedeOrigenCodigo().isBlank();

            if (!tieneSedeOrigen) {
                errors.add("La transferencia debe indicar sede origen");
            }
            if (!tieneSedeDestino) {
                errors.add("La transferencia debe indicar sede destino");
            }
            if (request.codigoServicio() != null && !request.codigoServicio().isBlank()) {
                errors.add("La transferencia entre sedes no debe indicar servicio");
            }
        }

        if (request.tipoMovimiento() == TipoMovimiento.TRANSFERENCIA_SERVICIO) {
            boolean tieneSedeOrigen = request.sedeOrigenCodigo() != null && !request.sedeOrigenCodigo().isBlank();
            boolean tieneServicio = request.codigoServicio() != null && !request.codigoServicio().isBlank();

            if (!tieneSedeOrigen) {
                errors.add("La transferencia a servicio debe indicar sede origen");
            }
            if (!tieneServicio) {
                errors.add("La transferencia a servicio debe indicar servicio destino");
            }
            if (request.sedeDestinoCodigo() != null && !request.sedeDestinoCodigo().isBlank()) {
                errors.add("La transferencia a servicio no debe indicar sede destino");
            }
        }

        if (request.tipoMovimiento() == TipoMovimiento.RETORNO_A_SEDE) {
            if (request.codigoServicio() == null || request.codigoServicio().isBlank()) {
                errors.add("El retorno a sede debe indicar servicio origen");
            }
            if (request.sedeDestinoCodigo() == null || request.sedeDestinoCodigo().isBlank()) {
                errors.add("El retorno a sede debe indicar sede destino");
            }
            if (request.codigoCuadrilla() != null && !request.codigoCuadrilla().isBlank()) {
                errors.add("El retorno a sede no debe indicar cuadrilla");
            }
        }

        if (request.tipoMovimiento() == TipoMovimiento.AJUSTE) {
            boolean tieneSede = request.sedeDestinoCodigo() != null && !request.sedeDestinoCodigo().isBlank();
            boolean tieneServicio = request.codigoServicio() != null && !request.codigoServicio().isBlank();

            if (tieneSede == tieneServicio) {
                errors.add("El ajuste debe indicar una sede o un servicio, pero no ambos");
            }
            if (request.sedeOrigenCodigo() != null && !request.sedeOrigenCodigo().isBlank()) {
                errors.add("El ajuste no debe indicar sede origen");
            }
            if (request.codigoCuadrilla() != null && !request.codigoCuadrilla().isBlank()) {
                errors.add("El ajuste no debe indicar cuadrilla");
            }
            if (request.observaciones() == null || request.observaciones().isBlank()) {
                errors.add("El ajuste debe indicar observaciones");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
