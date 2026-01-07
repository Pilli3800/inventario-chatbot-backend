package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioCreateRequest;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ItemRepository;
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

        // Cantidad
        if (request.cantidad() == null || request.cantidad() <= 0) {
            errors.add("La cantidad debe ser mayor a cero");
        }

        // Item existe y activo
        Item item = itemRepository
                .findByCodigoItem(request.codigoItem())
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

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}