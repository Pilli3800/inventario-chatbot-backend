package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaUpdateRequest;
import com.pilli3800.inventario.data.models.Cuadrilla;
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
public class CuadrillaUpdateValidator {

    private final CuadrillaRepository cuadrillaRepository;
    private final UserRepository userRepository;

    public void validate(String codigoCuadrilla, CuadrillaUpdateRequest request) {

        List<String> errors = new ArrayList<>();

        // 1️⃣ Cuadrilla existe
        Cuadrilla cuadrilla = cuadrillaRepository
                .findByCodigoCuadrilla(codigoCuadrilla)
                .orElse(null);

        if (cuadrilla == null) {
            errors.add("La cuadrilla no existe");
            throw new ValidationException(errors);
        }

        // 2️⃣ Cambio de jefe de cuadrilla
        if (request.codigoUsuario() != null) {

            User usuario = userRepository
                    .findByIdentUsuario(request.codigoUsuario())
                    .orElse(null);

            if (usuario == null) {
                errors.add("El usuario no existe");
            } else {

                boolean esJefeCuadrilla = usuario.getRoles().stream()
                        .anyMatch(r -> r.getNombreRol().equals("JEFE_CUADRILLA"));

                if (!esJefeCuadrilla) {
                    errors.add("El usuario no tiene el rol JEFE_CUADRILLA");
                }

                if (!usuario.isEnabled()) {
                    errors.add("El usuario está deshabilitado");
                }
            }
        }

        // 3️⃣ Cambio de estado (enabled)
        if (request.enabled() != null) {

            // No desactivar si ya está desactivada
            if (!request.enabled() && !cuadrilla.isEnabled()) {
                errors.add("La cuadrilla ya se encuentra desactivada");
            }

            // No activar si el jefe está desactivado
            if (request.enabled()
                    && !cuadrilla.isEnabled()
                    && !cuadrilla.getJefeCuadrilla().isEnabled()) {

                errors.add(
                        "No se puede activar la cuadrilla porque el jefe está deshabilitado"
                );
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}