package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.RegisterRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.RoleRepository;
import com.pilli3800.inventario.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Data
public class RegisterValidator {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void validate(RegisterRequest request) {

        List<String> errors = new ArrayList<>();

        if (userRepository.existsByIdentUsuario(request.identUsuario())) {
            errors.add("IdentUsuario ya existe");
        }

        if (userRepository.existsByDni(request.dni())) {
            errors.add("DNI ya existe");
        }

        if (userRepository.existsByEmail(request.email())) {
            errors.add("Email ya existe");
        }

        List<String> invalidRoles = request.roles().stream()
                .filter(nombre -> !roleRepository.existsByNombreRol(nombre))
                .toList();

        if (!invalidRoles.isEmpty()) {
            errors.add("Rol no existe: " + String.join(", ", invalidRoles));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
