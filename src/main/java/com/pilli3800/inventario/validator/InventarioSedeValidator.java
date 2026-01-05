package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.InventarioSedeCreateRequest;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.InventarioSedeRepository;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioSedeValidator {

    private final ItemRepository itemRepository;
    private final SedeRepository sedeRepository;
    private final InventarioSedeRepository inventarioSedeRepository;

    public void validate(InventarioSedeCreateRequest request) {

        List<String> errors = new ArrayList<>();

        Item item = itemRepository.findByCodigoItem(request.codigoItem())
                .orElse(null);

        if (item == null) {
            errors.add("El item no existe");
        } else if (!item.isEnabled()) {
            errors.add("El item está desactivado");
        }

        Sede sede = sedeRepository.findByCodigo(request.sedeCodigo())
                .orElse(null);

        if (sede == null) {
            errors.add("La sede no existe");
        } else if (!sede.isEnabled()) {
            errors.add("La sede está desactivada");
        }

        if (item != null && sede != null) {
            boolean exists = inventarioSedeRepository
                    .existsByItemAndSede(item, sede);

            if (exists) {
                errors.add("El item ya está asignado a esta sede");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}