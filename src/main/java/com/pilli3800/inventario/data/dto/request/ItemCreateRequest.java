package com.pilli3800.inventario.data.dto.request;

import com.pilli3800.inventario.data.models.enums.TipoItem;

public record ItemCreateRequest(
        TipoItem tipo,
        String nombre,
        String descripcion,
        String codigoItem,
        String observaciones
) { }
