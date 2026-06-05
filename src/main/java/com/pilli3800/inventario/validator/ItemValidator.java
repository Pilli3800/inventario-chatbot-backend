package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.ItemCreateRequest;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemValidator {

    private final ItemRepository itemRepository;

    public void validate(ItemCreateRequest request) {

        List<String> errors = new ArrayList<>();

        // Código único
        String codigoItem = TextNormalizer.normalizeCode(request.codigoItem());
        if (itemRepository.existsByCodigoItem(codigoItem)) {
            errors.add("El código de item ya existe");
        }

        // Tipo obligatorio
        if (request.tipo() == null) {
            errors.add("El tipo de item es obligatorio");
        }

        // Nombre obligatorio
        if (request.nombre() == null || request.nombre().isBlank()) {
            errors.add("El nombreServicio del item es obligatorio");
        }

        if (request.stockMinimo() == null || request.stockMinimo() <= 0) {
            errors.add("El stock minimo del item debe ser mayor que 0");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
