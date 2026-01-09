package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ServicioUpdateValidator {

    private final ServicioRepository servicioRepository;

    public void validate(String codigo) {

        if (!servicioRepository.existsByCodigo(codigo)) {
            throw new ValidationException(List.of("El servicio no existe"));
        }
    }
}