package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.ProveedorCreateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProveedorValidator {

    private final ProveedorRepository proveedorRepository;

    public void validate(ProveedorCreateRequest request) {

        List<String> errors = new ArrayList<>();

        if (request.ruc() != null && proveedorRepository.existsByRuc(request.ruc())) {
            errors.add("El ruc ya existe");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
