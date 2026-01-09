package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaUpdateRequest;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.CuadrillaRepository;
import com.pilli3800.inventario.repository.ServicioRepository;
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
    private final ServicioRepository servicioRepository;

    public void validate(String codigoCuadrilla, CuadrillaUpdateRequest request) {

        List<String> errors = new ArrayList<>();

        // Cuadrilla existe
        Cuadrilla cuadrilla = cuadrillaRepository
                .findByCodigoCuadrilla(codigoCuadrilla)
                .orElse(null);

        if (cuadrilla == null) {
            errors.add("La cuadrilla no existe");
            throw new ValidationException(errors);
        }

        // Cambio de jefe de cuadrilla
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

        // Servicio
        if (request.codigoServicio() != null) {

            Servicio servicio = servicioRepository
                    .findByCodigo(request.codigoServicio())
                    .orElse(null);

            if (servicio == null) {
                errors.add("El servicio no existe");
            }

            if (servicio != null && !servicio.isEnabled()) {
                errors.add("El servicio está deshabilitado");
            }
        }

        // Cambio de estado (enabled)
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
                        "No se puede activar la cuadrilla porque el jefe de cuadrilla está deshabilitado"
                );
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}