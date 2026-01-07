package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaCreateRequest;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.CuadrillaRepository;
import com.pilli3800.inventario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CuadrillaValidator {

    private final CuadrillaRepository cuadrillaRepository;
    private final UserRepository userRepository;

    public void validate(CuadrillaCreateRequest request) {

        List<String> errors = new ArrayList<>();

        // Código de cuadrilla único
        if (cuadrillaRepository.existsByCodigoCuadrilla(request.codigoCuadrilla())) {
            errors.add("El código de la cuadrilla ya existe");
        }

        // Buscar usuario
        User usuario = userRepository
                .findByIdentUsuario(request.codigoUsuario())
                .orElse(null);

        if (usuario == null) {
            errors.add("El usuario no existe");
        } else {
            // Validar rol JEFE_CUADRILLA
            boolean esJefeCuadrilla = usuario.getRoles().stream()
                    .anyMatch(role -> role.getNombreRol().equals("JEFE_CUADRILLA"));

            if (!esJefeCuadrilla) {
                errors.add("El usuario no tiene el rol JEFE_CUADRILLA");
            }

            if (!usuario.isEnabled()) {
                errors.add("El usuario está deshabilitado");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}