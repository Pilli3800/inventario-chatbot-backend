package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioSedeDeleteValidator {

    public void validate(InventarioSede inventarioSede) {

        List<String> errors = new ArrayList<>();

        if (inventarioSede.getStock() > 0) {
            errors.add("No se puede eliminar la asignación porque el stock es mayor a cero");
        }

        //Falta validar que el item no tenga movimientos para poder eliminarse, solo se valida que el stock sea 0

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}