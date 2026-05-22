package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoCreateRequest;
import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoDetalleRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.util.TextNormalizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ConteoFisicoCreateValidator {

    public void validate(ConteoFisicoCreateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.tipoInventario() == null) {
            errors.add("El tipo de inventario es obligatorio");
        }

        if (request.codigoUbicacion() == null || request.codigoUbicacion().isBlank()) {
            errors.add("El codigo de ubicacion es obligatorio");
        }

        if (request.detalles() == null || request.detalles().isEmpty()) {
            errors.add("El conteo debe tener al menos un item");
        } else {
            Set<String> codigos = new HashSet<>();
            for (ConteoFisicoDetalleRequest detalle : request.detalles()) {
                if (detalle == null) {
                    errors.add("Los items del conteo no son validos");
                    continue;
                }

                if (detalle.codigoItem() == null || detalle.codigoItem().isBlank()) {
                    errors.add("El codigo de item es obligatorio");
                    continue;
                }

                String codigoItem = TextNormalizer.normalizeCode(detalle.codigoItem());
                if (!codigos.add(codigoItem)) {
                    errors.add("No se permiten items duplicados en el conteo");
                    continue;
                }

                if (detalle.stockSistema() == null) {
                    errors.add("El stock del sistema es obligatorio para el item: " + codigoItem);
                } else if (detalle.stockSistema() < 0) {
                    errors.add("El stock del sistema no puede ser negativo para el item: " + codigoItem);
                }

                if (detalle.cantidadFisica() == null) {
                    errors.add("La cantidad fisica es obligatoria para el item: " + codigoItem);
                } else if (detalle.cantidadFisica() < 0) {
                    errors.add("La cantidad fisica debe ser mayor o igual a 0 para el item: " + codigoItem);
                }
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            errors.add("Usuario no autenticado");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
