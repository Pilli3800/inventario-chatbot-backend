package com.pilli3800.inventario.data.dto.request;

import com.pilli3800.inventario.data.models.enums.TipoItem;

public record ItemSearchRequest(
        String nombre,
        String codigoItem,
        TipoItem tipo,
        Boolean enabled
) { }

