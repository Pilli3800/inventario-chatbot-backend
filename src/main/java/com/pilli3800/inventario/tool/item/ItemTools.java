package com.pilli3800.inventario.tool.item;

import com.pilli3800.inventario.data.dto.request.ItemSearchRequest;
import com.pilli3800.inventario.data.dto.response.ItemDto;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemTools {

    private final ItemService itemService;

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

        return "- Codigo: " + item.codigoItem()
                + ", nombre: " + item.nombre()
                + ", tipo: " + item.tipo()
                + descripcion
                + ", estado: " + (item.enabled() ? "activo" : "inactivo");
    }
}
