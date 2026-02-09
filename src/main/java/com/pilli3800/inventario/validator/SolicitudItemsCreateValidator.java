package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.solicituditems.SolicitudItemsCreateRequest;
import com.pilli3800.inventario.data.dto.request.solicituditems.SolicitudItemsDetalleRequest;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SolicitudItemsCreateValidator {

    private final ItemRepository itemRepository;

    public void validate(SolicitudItemsCreateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.codigoCuadrilla() == null || request.codigoCuadrilla().isBlank()) {
            errors.add("El codigo de cuadrilla es obligatorio");
        }

        if (request.sedeOrigenCodigo() == null || request.sedeOrigenCodigo().isBlank()) {
            errors.add("El codigo de sede origen es obligatorio");
        }

        if (request.detalles() == null || request.detalles().isEmpty()) {
            errors.add("La solicitud debe tener al menos un item");
        } else {
            Set<String> codigos = new HashSet<>();
            for (SolicitudItemsDetalleRequest itemRequest : request.detalles()) {
                if (itemRequest == null) {
                    errors.add("Los items de la solicitud no son validos");
                    continue;
                }

                if (itemRequest.codigoItem() == null || itemRequest.codigoItem().isBlank()) {
                    errors.add("El codigo de item es obligatorio");
                    continue;
                }

                String codigoBusqueda = TextNormalizer.normalizeCode(itemRequest.codigoItem());
                if (!codigos.add(codigoBusqueda)) {
                    errors.add("No se permiten items duplicados en la solicitud");
                    continue;
                }

                if (itemRequest.cantidad() == null || itemRequest.cantidad() <= 0) {
                    errors.add("La cantidad debe ser mayor a 0 para el item: " + codigoBusqueda);
                    continue;
                }

                Item item = itemRepository
                        .findByCodigoItem(codigoBusqueda)
                        .orElse(null);

                if (item == null) {
                    errors.add("El item no existe: " + codigoBusqueda);
                } else if (!item.isEnabled()) {
                    errors.add("El item esta desactivado: " + codigoBusqueda);
                }
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            errors.add("Usuario no autenticado");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
