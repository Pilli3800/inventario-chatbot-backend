package com.pilli3800.inventario.data.dto.request;

import com.pilli3800.inventario.data.models.enums.TipoItem;

import java.math.BigDecimal;

public record ItemUpdateRequest(
        String nombre,
        String descripcion,
        TipoItem tipo,
        Boolean enabled,
        Long stockMinimo,
        String observaciones
) { }

