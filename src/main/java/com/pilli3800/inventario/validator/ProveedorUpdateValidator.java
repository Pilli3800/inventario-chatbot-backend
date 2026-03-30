package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.ProveedorUpdateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProveedorUpdateValidator {

    private final ProveedorRepository proveedorRepository;

    public void validate(Long proveedorId, ProveedorUpdateRequest request) {

        List<String> errors = new ArrayList<>();

        if (request.ruc() != null && request.ruc().isBlank()) {
            errors.add("El ruc no puede estar vacio");
        }

        if (request.nombre() != null && request.nombre().isBlank()) {
            errors.add("El nombre del proveedor no puede estar vacio");
        }

        if (request.telefono() != null && request.telefono().length() > 9) {
            errors.add("El telefono no puede exceder 9 digitos");
        }

        if (request.observaciones() != null && request.observaciones().length() > 255) {
            errors.add("Las observaciones son muy largas, maximo 255 caracteres");
        }

        if (request.ruc() != null) {
            proveedorRepository.findByRuc(request.ruc())
                    .filter(p -> !p.getId().equals(proveedorId))
                    .ifPresent(p -> errors.add("El ruc ya existe"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
