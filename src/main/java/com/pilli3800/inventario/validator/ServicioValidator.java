package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.servicios.ServicioCreateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServicioValidator {

    private final ServicioRepository servicioRepository;

    public void validateCreate(ServicioCreateRequest request) {

        List<String> errors = new ArrayList<>();

        if (servicioRepository.existsByCodigo(request.codigoServicio())) {
            errors.add("El código del servicio ya existe");
        }

        if (request.nombreServicio() == null || request.nombreServicio().isBlank()) {
            errors.add("El nombre es obligatorio");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}