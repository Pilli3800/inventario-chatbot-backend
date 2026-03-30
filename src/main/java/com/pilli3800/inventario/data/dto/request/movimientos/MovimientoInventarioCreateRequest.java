package com.pilli3800.inventario.data.dto.request.movimientos;

import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record MovimientoInventarioCreateRequest(

        @NotNull
        TipoMovimiento tipoMovimiento,

        @NotBlank
        String codigoItem,

        // Obligatoria segun tipoMovimiento (SALIDA / TRANSFERENCIA / TRANSFERENCIA_SERVICIO)
        String sedeOrigenCodigo,

        // Obligatoria segun tipoMovimiento (ENTRADA / TRANSFERENCIA / COMPRA / RETORNO_A_SEDE)
        String sedeDestinoCodigo,

        // Obligatorio segun tipoMovimiento (TRANSFERENCIA_SERVICIO / RETORNO_A_SEDE)
        String codigoServicio,

        // Obligatorio segun tipoMovimiento (COMPRA)
        String codigoProveedor,

        // Opcional segun tipoMovimiento (COMPRA): usar cuando la factura ya existe
        String numeroFactura,

        // Opcional segun tipoMovimiento (COMPRA): usar junto con correlativoFactura para factura nueva
        String serieFactura,

        // Opcional segun tipoMovimiento (COMPRA): usar junto con serieFactura para factura nueva
        String correlativoFactura,

        // Opcional segun tipoMovimiento (COMPRA)
        LocalDate fechaEmisionFactura,

        @NotNull
        @Positive
        Long cantidad,

        String observaciones,

        String codigoCuadrilla

) { }
