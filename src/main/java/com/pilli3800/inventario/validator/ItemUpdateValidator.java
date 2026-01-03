package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.ItemUpdateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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