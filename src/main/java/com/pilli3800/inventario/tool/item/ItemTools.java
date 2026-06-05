package com.pilli3800.inventario.tool.item;

import com.pilli3800.inventario.data.dto.request.ItemSearchRequest;
import com.pilli3800.inventario.data.dto.request.InventarioSedeSearchRequest;
import com.pilli3800.inventario.data.dto.request.InventarioServicioSearchRequest;
import com.pilli3800.inventario.data.dto.response.InventarioSedeDto;
import com.pilli3800.inventario.data.dto.response.InventarioServicioDto;
import com.pilli3800.inventario.data.dto.response.ItemDto;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.InventarioSedeService;
import com.pilli3800.inventario.service.InventarioServicioService;
import com.pilli3800.inventario.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemTools {

    private final ItemService itemService;
    private final InventarioSedeService inventarioSedeService;
    private final InventarioServicioService inventarioServicioService;

    @Tool(description = "Busca items activos por nombre. Usala cuando el usuario pregunte por un nombre o una descripcion aproximada del item.")
    @PreAuthorize("isAuthenticated()")
    public String buscarItemsPorNombre(String nombre) {
        return responder(new ItemSearchRequest(nombre, null, null, true));
    }

    @Tool(description = "Busca items activos cuyo nombre empiece con un texto especifico. Usala cuando el usuario diga 'empieza con', 'inicia con' o pida items por letra inicial.")
    @PreAuthorize("isAuthenticated()")
    public String buscarItemsPorTextoInicial(String textoInicial) {
        List<ItemDto> items = itemService.getItemsByTextoInicial(
                textoInicial,
                PageRequest.of(0, 10, Sort.by("codigoItem"))
        ).getContent();

        if (items.isEmpty()) {
            return "No se encontraron items cuyo nombre empiece con el texto enviado.";
        }

        return "Se encontraron " + items.size() + " item(s):" + System.lineSeparator() +
                items.stream()
                        .map(this::formatearItem)
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    @Tool(description = "Busca un item por codigo exacto o parcial. Usala cuando el usuario mencione un codigo de item.")
    @PreAuthorize("isAuthenticated()")
    public String buscarItemPorCodigo(String codigoItem) {
        return responder(new ItemSearchRequest(null, codigoItem, null, null));
    }

    @Tool(description = "Busca items activos por tipo. El tipo debe ser MATERIAL, HERRAMIENTA o EQUIPO.")
    @PreAuthorize("isAuthenticated()")
    public String buscarItemsPorTipo(String tipo) {
        return responder(new ItemSearchRequest(null, null, TipoItem.valueOf(tipo.trim().toUpperCase()), true));
    }

    @Tool(description = "Lista los items criticos en una sede, comparando el stock actual con el stock minimo del item.")
    @PreAuthorize("isAuthenticated()")
    public String listarItemsCriticosPorSede(String sedeCodigo) {
        List<InventarioSedeDto> inventarios = inventarioSedeService.getInventario(
                new InventarioSedeSearchRequest(sedeCodigo, null, null, null, null, null),
                PageRequest.of(0, 1000)
        ).getContent();

        List<String> criticos = new ArrayList<>();
        for (InventarioSedeDto inventario : inventarios) {
            ItemDto item = itemService.getItem(inventario.codigoItem());
            if (esCritico(item.stockMinimo(), inventario.stock())) {
                criticos.add(formatearCritico(
                        inventario.codigoItem(),
                        inventario.nombreItem(),
                        inventario.stock(),
                        item.stockMinimo(),
                        "sede " + sedeCodigo
                ));
            }
        }

        criticos.sort(String::compareToIgnoreCase);
        return construirRespuestaCriticos("sede " + sedeCodigo, criticos);
    }

    @Tool(description = "Lista los items criticos en un servicio, comparando el stock actual con el stock minimo del item.")
    @PreAuthorize("isAuthenticated()")
    public String listarItemsCriticosPorServicio(String codigoServicio) {
        List<InventarioServicioDto> inventarios = inventarioServicioService.getInventario(
                new InventarioServicioSearchRequest(codigoServicio, null, null, null, null, null),
                PageRequest.of(0, 1000)
        ).getContent();

        List<String> criticos = new ArrayList<>();
        for (InventarioServicioDto inventario : inventarios) {
            ItemDto item = itemService.getItem(inventario.codigoItem());
            if (esCritico(item.stockMinimo(), inventario.stockActual())) {
                criticos.add(formatearCritico(
                        inventario.codigoItem(),
                        inventario.nombreItem(),
                        inventario.stockActual(),
                        item.stockMinimo(),
                        "servicio " + codigoServicio
                ));
            }
        }

        criticos.sort(String::compareToIgnoreCase);
        return construirRespuestaCriticos("servicio " + codigoServicio, criticos);
    }

    private String responder(ItemSearchRequest request) {
        List<ItemDto> items = itemService.getItems(
                request,
                PageRequest.of(0, 10, Sort.by("codigoItem"))
        ).getContent();

        if (items.isEmpty()) {
            return "No se encontraron items con los filtros enviados.";
        }

        return "Se encontraron " + items.size() + " item(s):" + System.lineSeparator() +
                items.stream()
                        .map(this::formatearItem)
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatearItem(ItemDto item) {
        String descripcion = item.descripcion() == null || item.descripcion().isBlank()
                ? ""
                : ", descripcion: " + item.descripcion().trim();

        String stockMinimo = item.stockMinimo() == null
                ? ""
                : ", stock minimo: " + item.stockMinimo();

        return "- Codigo: " + item.codigoItem()
                + ", nombre: " + item.nombre()
                + ", tipo: " + item.tipo()
                + descripcion
                + stockMinimo
                + ", estado: " + (item.enabled() ? "activo" : "inactivo");
    }

    private boolean esCritico(Long stockMinimo, Long stockActual) {
        return stockMinimo != null && stockActual != null && stockActual <= stockMinimo;
    }

    private String formatearCritico(
            String codigoItem,
            String nombreItem,
            Long stockActual,
            Long stockMinimo,
            String ubicacion
    ) {
        return "- Codigo: " + codigoItem
                + ", nombre: " + nombreItem
                + ", stock actual: " + stockActual
                + ", stock minimo: " + stockMinimo
                + ", ubicacion: " + ubicacion;
    }

    private String construirRespuestaCriticos(String ubicacion, List<String> criticos) {
        if (criticos.isEmpty()) {
            return "No se encontraron items criticos en " + ubicacion + ".";
        }

        return "Se encontraron " + criticos.size() + " item(s) criticos en " + ubicacion + ":"
                + System.lineSeparator()
                + criticos.stream().collect(Collectors.joining(System.lineSeparator()));
    }
}
