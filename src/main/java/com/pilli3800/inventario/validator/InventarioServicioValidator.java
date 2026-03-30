package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.InventarioServicioCreateRequest;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.InventarioServicioRepository;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.repository.ServicioRepository;
import com.pilli3800.inventario.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioServicioValidator {

    private final ItemRepository itemRepository;
    private final ServicioRepository servicioRepository;
    private final InventarioServicioRepository inventarioServicioRepository;

    public void validate(InventarioServicioCreateRequest request) {

        List<String> errors = new ArrayList<>();

        String codigoItem = TextNormalizer.normalizeCode(request.codigoItem());
        Item item = itemRepository.findByCodigoItem(codigoItem)
                .orElse(null);

        if (item == null) {
            errors.add("El item no existe");
        } else if (!item.isEnabled()) {
            errors.add("El item esta desactivado");
        }

        String codigoServicio = TextNormalizer.normalizeCode(request.servicioCodigo());
        Servicio servicio = servicioRepository.findByCodigo(codigoServicio)
                .orElse(null);

        if (servicio == null) {
            errors.add("El servicio no existe");
        } else if (!servicio.isEnabled()) {
            errors.add("El servicio esta desactivado");
        }

        if (item != null && servicio != null) {
            boolean exists = inventarioServicioRepository.existsByItemAndServicio(item, servicio);
            if (exists) {
                errors.add("El item ya esta asignado a este servicio");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
