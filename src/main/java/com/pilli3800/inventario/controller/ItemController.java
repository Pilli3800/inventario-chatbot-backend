package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.ItemCreateRequest;
import com.pilli3800.inventario.data.dto.request.ItemSearchRequest;
import com.pilli3800.inventario.data.dto.request.ItemUpdateRequest;
import com.pilli3800.inventario.data.dto.response.ItemDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import com.pilli3800.inventario.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistica/items")
@PreAuthorize("hasRole('LOGISTICA')")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{codigoItem}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ItemDto> getItem(@PathVariable String codigoItem) {
        return new SingleResponse<>(
                200,
                "/api/logistica/item/{codigoItem}",
                itemService.getItem(codigoItem)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ItemDto> getItems(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String codigoItem,
            @RequestParam(required = false) TipoItem tipo,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(page = 0, size = 5, sort = "nombre") Pageable pageable
    ) {

        ItemSearchRequest request = new ItemSearchRequest(
                nombre,
                codigoItem,
                tipo,
                enabled
        );

        Page<ItemDto> page = itemService.getItems(request, pageable);

        return PageResponse.from(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<ItemDto> createItem(
            @RequestBody ItemCreateRequest request
    ) {
        return new SingleResponse<>(
                201,
                "/api/logistica/item",
                itemService.createItem(request)
        );
    }

    @PutMapping("/{codigoItem}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ItemDto> updateItem(
            @PathVariable String codigoItem,
            @RequestBody ItemUpdateRequest request
    ) {
        return new SingleResponse<>(
                200,
                "/api/logistica/item/{codigoItem}",
                itemService.updateItem(codigoItem, request)
        );
    }

    @PatchMapping("/{codigoItem}/desactivar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> disableItem(@PathVariable String codigoItem) {
        itemService.disableItem(codigoItem);
        return new SingleResponse<>(
                200,
                "/api/logistica/items/" + codigoItem + "/desactivar",
                "Item desactivado"
        );
    }

    @PatchMapping("/{codigoItem}/activar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> enableItem(@PathVariable String codigoItem) {
        itemService.enableItem(codigoItem);
        return new SingleResponse<>(
                200,
                "/api/logistica/items/" + codigoItem + "/activar",
                "Item activado"
        );
    }

}