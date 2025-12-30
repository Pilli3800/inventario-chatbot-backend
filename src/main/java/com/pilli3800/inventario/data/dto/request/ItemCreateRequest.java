package com.pilli3800.inventario.data.dto.request;

import com.pilli3800.inventario.data.models.enums.TipoItem;

import java.math.BigDecimal;

public record ItemCreateRequest(
        TipoItem tipo,
        String nombre,
        String descripcion,
        String codigoItem,
        BigDecimal stockInicial,
        String observaciones
) { }
