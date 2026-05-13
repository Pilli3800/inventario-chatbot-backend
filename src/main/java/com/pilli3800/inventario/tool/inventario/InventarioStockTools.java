package com.pilli3800.inventario.tool.inventario;

import com.pilli3800.inventario.service.InventarioSedeService;
import com.pilli3800.inventario.service.InventarioServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventarioStockTools {

    private final InventarioSedeService inventarioSedeService;
    private final InventarioServicioService inventarioServicioService;

    @Tool(description = """
            Consulta si hay stock disponible de un item en una sede o en un servicio.
            Usala cuando el usuario pregunte por stock disponible y mencione una sede o un servicio.
            El parametro tipoInventario debe ser SEDE o SERVICIO.
            El parametro codigoUbicacion debe ser el codigo de la sede o del servicio.
            El parametro codigoItem debe ser el codigo exacto del item.
            Si el usuario solo menciona el nombre del item, primero busca el item y luego usa esta tool con el codigo encontrado.
            """)
    @PreAuthorize("isAuthenticated()")
    public String consultarStockDisponible(
            String codigoItem,
            String tipoInventario,
            String codigoUbicacion
    ) {
        String tipo = tipoInventario.trim().toUpperCase();

        long stock = switch (tipo) {
            case "SEDE" -> inventarioSedeService.obtenerStockPorSede(codigoUbicacion, codigoItem);
            case "SERVICIO" -> inventarioServicioService.obtenerStockPorServicio(codigoUbicacion, codigoItem);
            default -> throw new RuntimeException("El tipoInventario debe ser SEDE o SERVICIO");
        };

        if (stock > 0) {
            return "Si hay stock disponible del item " + codigoItem
                    + " en " + tipo.toLowerCase()
                    + " " + codigoUbicacion + ". Stock actual: " + stock + ".";
        }

        return "No hay stock disponible del item " + codigoItem
                + " en " + tipo.toLowerCase()
                + " " + codigoUbicacion + ".";
    }
}
