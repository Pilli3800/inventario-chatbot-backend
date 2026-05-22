package com.pilli3800.inventario.data.dto.request.ia;

import java.util.List;

public record ChatScreenContextRequest(
        String ruta,
        String titulo,
        String modulo,
        List<String> elementosVisibles,
        List<String> accionesDisponibles
) {
}
