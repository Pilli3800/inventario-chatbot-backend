package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.UserUpdateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.RoleRepository;
import com.pilli3800.inventario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserUpdateValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void validate(Long userId, UserUpdateRequest request) {

        List<String> errors = new ArrayList<>();

        // IdentUsuario duplicado
        if (userRepository.existsByIdentUsuarioAndIdNot(request.identUsuario(), userId)) {
            errors.add("IdentUsuario ya está en uso.");
        }

        // DNI duplicado
        if (userRepository.existsByDniAndIdNot(request.dni(), userId)) {
            errors.add("DNI ya está en uso.");
        }

        // Email duplicado
        if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
            errors.add("Email ya está en uso.");
        }

        // Validar roles
        List<String> invalidRoles = request.roles().stream()
                .filter(nombre -> !roleRepository.existsByNombreRol(nombre))
                .toList();

        if (!invalidRoles.isEmpty()) {
            errors.add("Roles inválidos: " + String.join(", ", invalidRoles));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}

