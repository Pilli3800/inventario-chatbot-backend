package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.ItemCreateRequest;
import com.pilli3800.inventario.data.dto.request.ItemSearchRequest;
import com.pilli3800.inventario.data.dto.request.ItemUpdateRequest;
import com.pilli3800.inventario.data.dto.response.ItemDto;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.specifications.ItemSpecifications;
import com.pilli3800.inventario.validator.ItemUpdateValidator;
import com.pilli3800.inventario.validator.ItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;
    private final ItemUpdateValidator itemUpdateValidator;

    public ItemDto getItem(String codigoItem) {
        return itemRepository.findByCodigoItem(codigoItem).map(ItemDto::from).orElseThrow(() -> new RuntimeException("Item no encontrado"));
    }

    public Page<ItemDto> getItems(ItemSearchRequest request, Pageable pageable) {
        Specification<Item> spec = ItemSpecifications.search(request);
        Page<Item> page = itemRepository.findAll(spec, pageable);
        return page.map(ItemDto::from);
    }

    public ItemDto createItem(ItemCreateRequest request) {

        itemValidator.validate(request);

        Item item = new Item();
        item.setTipo(request.tipo());
        item.setNombre(request.nombre());
        item.setDescripcion(request.descripcion());
        item.setCodigoItem(request.codigoItem());
        item.setStockTotal(request.stockInicial());
        item.setStockDisponible(request.stockInicial());
        item.setEnabled(true);
        item.setObservaciones(request.observaciones());

        return ItemDto.from(itemRepository.save(item));
    }

    public ItemDto updateItem(String codigoItem, ItemUpdateRequest request) {

        Item item = itemRepository.findByCodigoItem(codigoItem)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        itemUpdateValidator.validate(item.getId(), request);

        item.setTipo(request.tipo());
        item.setNombre(request.nombre());
        item.setDescripcion(request.descripcion());
        item.setStockTotal(request.stockTotal());
        item.setStockDisponible(request.stockDisponible());
        item.setEnabled(request.enabled());
        item.setObservaciones(request.observaciones());

        return ItemDto.from(itemRepository.save(item));
    }

    private void setItemEnabled(String codigoItem, boolean enabled) {
        Item item = itemRepository.findByCodigoItem(codigoItem)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        item.setEnabled(enabled);
        itemRepository.save(item);
    }

    public void enableItem(String codigoItem) {
        setItemEnabled(codigoItem, true);
    }

    public void disableItem(String codigoItem) {
        setItemEnabled(codigoItem, false);
    }

}
