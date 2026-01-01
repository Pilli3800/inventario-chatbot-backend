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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


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

    public byte[] exportItemsToExcel(ItemSearchRequest request) throws IOException {

        Specification<Item> spec = ItemSpecifications.search(request);
        List<Item> items = itemRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Items");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);

        Cell h0 = header.createCell(0);
        h0.setCellValue("ID");
        h0.setCellStyle(headerStyle);

        Cell h1 = header.createCell(1);
        h1.setCellValue("Tipo");
        h1.setCellStyle(headerStyle);

        Cell h2 = header.createCell(2);
        h2.setCellValue("Nombre");
        h2.setCellStyle(headerStyle);

        Cell h3 = header.createCell(3);
        h3.setCellValue("Código");
        h3.setCellStyle(headerStyle);

        Cell h4 = header.createCell(4);
        h4.setCellValue("Stock Total");
        h4.setCellStyle(headerStyle);

        Cell h5 = header.createCell(5);
        h5.setCellValue("Stock Disponible");
        h5.setCellStyle(headerStyle);

        Cell h6 = header.createCell(6);
        h6.setCellValue("Activo");
        h6.setCellStyle(headerStyle);

        Cell h7 = header.createCell(7);
        h7.setCellValue("Observaciones");
        h7.setCellStyle(headerStyle);

        int rowIdx = 1;
        for (Item item : items) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId());
            row.createCell(1).setCellValue(item.getTipo().name());
            row.createCell(2).setCellValue(item.getNombre());
            row.createCell(3).setCellValue(item.getCodigoItem());
            row.createCell(4).setCellValue(item.getStockTotal().doubleValue());
            row.createCell(5).setCellValue(item.getStockDisponible().doubleValue());
            row.createCell(6).setCellValue(item.isEnabled());
            row.createCell(7).setCellValue(item.getObservaciones());
        }

        for (int i = 0; i <= 7; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

}
