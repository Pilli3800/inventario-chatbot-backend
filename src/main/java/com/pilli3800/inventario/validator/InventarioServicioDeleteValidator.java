package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.MovimientoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioServicioDeleteValidator {

    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public void validate(InventarioServicio inventarioServicio) {

        List<String> errors = new ArrayList<>();

        if (inventarioServicio.getStockActual() > 0) {
            errors.add("No se puede eliminar la asignacion porque el stock es mayor a cero");
        }

        boolean tieneMovimientos =
                movimientoInventarioRepository.existsByInventarioServicioOrigen(inventarioServicio)
                        || movimientoInventarioRepository.existsByInventarioServicioDestino(inventarioServicio);

        if (tieneMovimientos) {
            errors.add("No se puede eliminar la asignacion porque existen movimientos de inventario asociados");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
