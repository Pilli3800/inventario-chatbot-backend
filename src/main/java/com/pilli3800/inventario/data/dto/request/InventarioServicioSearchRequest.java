package com.pilli3800.inventario.data.dto.request;

import com.pilli3800.inventario.data.models.enums.TipoItem;

public record InventarioServicioSearchRequest(
        String codigoServicio,
        String nombreItem,
        String codigoItem,
        TipoItem tipoItem,
        Boolean enabledItem,
        Boolean conStock
) { }
