package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.ItemUpdateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemUpdateValidator {

    public void validate(Long itemId, ItemUpdateRequest request) {

        List<String> errors = new ArrayList<>();

        if (request.nombre() != null && request.nombre().isBlank()) {
            errors.add("El nombre del item no puede estar vacío");
        }

        if (request.stockTotal() != null &&
                request.stockTotal().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("El stock total no puede ser negativo");
        }

        if (request.stockDisponible() != null &&
                request.stockDisponible().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("El stock disponible no puede ser negativo");
        }

        if (request.stockTotal() != null && request.stockDisponible() != null &&
                request.stockDisponible().compareTo(request.stockTotal()) > 0) {
            errors.add("El stock disponible no puede ser mayor al stock total");
        }

        // Descripcion
        if (request.descripcion().length() > 255) {
            errors.add("La descripcion del item es muy larga, maximo 255 caracteres");
        }

        // Observaciones
        if (request.observaciones().length() > 255) {
            errors.add("Las observaciones del item es muy larga, maximo 255 caracteres");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}