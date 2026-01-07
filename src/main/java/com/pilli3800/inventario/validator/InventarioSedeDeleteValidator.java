package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.MovimientoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioSedeDeleteValidator {

    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public void validate(InventarioSede inventarioSede) {

        List<String> errors = new ArrayList<>();

        if (inventarioSede.getStock() > 0) {
            errors.add("No se puede eliminar la asignación porque el stock es mayor a cero");
        }
        // Item no debe tener movimientos
        boolean tieneMovimientos =
                movimientoInventarioRepository
                        .existsByInventarioOrigen(inventarioSede)
                        || movimientoInventarioRepository
                        .existsByInventarioDestino(inventarioSede);

        if (tieneMovimientos) {
            errors.add(
                    "No se puede eliminar la asignación porque existen movimientos de inventario asociados"
            );
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}