package com.pilli3800.inventario.data.dto.request.movimientos;

import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovimientoInventarioCreateRequest(

        @NotNull
        TipoMovimiento tipoMovimiento,

        @NotBlank
        String codigoItem,

        // Obligatoria según tipoMovimiento (SALIDA / TRANSFERENCIA)
        String sedeOrigenCodigo,

        // Obligatoria según tipoMovimiento (ENTRADA / TRANSFERENCIA / DEVOLUCION)
        String sedeDestinoCodigo,

        @NotNull
        @Positive
        Long cantidad,

        String observaciones

) { }