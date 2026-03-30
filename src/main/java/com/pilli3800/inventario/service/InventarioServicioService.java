package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.InventarioServicioCreateRequest;
import com.pilli3800.inventario.data.dto.request.InventarioServicioSearchRequest;
import com.pilli3800.inventario.data.dto.response.InventarioServicioDto;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.InventarioServicioRepository;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.repository.ServicioRepository;
import com.pilli3800.inventario.specifications.InventarioServicioSpecifications;
import com.pilli3800.inventario.util.TextNormalizer;
import com.pilli3800.inventario.validator.InventarioServicioDeleteValidator;
import com.pilli3800.inventario.validator.InventarioServicioValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServicioService {

    private final InventarioServicioRepository inventarioServicioRepository;
    private final ServicioRepository servicioRepository;
    private final ItemRepository itemRepository;
    private final InventarioSedeService inventarioSedeService;
    private final InventarioServicioValidator inventarioServicioValidator;
    private final InventarioServicioDeleteValidator inventarioServicioDeleteValidator;

    public Page<InventarioServicioDto> getInventario(
            InventarioServicioSearchRequest request,
            Pageable pageable
    ) {
        Specification<InventarioServicio> spec =
                InventarioServicioSpecifications.search(request);

        return inventarioServicioRepository
                .findAll(spec, pageable)
                .map(InventarioServicioDto::from);
    }

    public void asignarItemAServicio(InventarioServicioCreateRequest request) {
        inventarioServicioValidator.validate(request);

        String codigoItem = TextNormalizer.normalizeCode(request.codigoItem());
        String codigoServicio = TextNormalizer.normalizeCode(request.servicioCodigo());

        Item item = itemRepository.findByCodigoItem(codigoItem).get();
        Servicio servicio = servicioRepository.findByCodigo(codigoServicio).get();

        InventarioServicio inventario = new InventarioServicio();
        inventario.setItem(item);
        inventario.setServicio(servicio);
        inventario.setStockActual(0L);

        inventarioServicioRepository.save(inventario);
    }

    public void eliminarAsignacion(Long inventarioServicioId) {
        InventarioServicio inventarioServicio = inventarioServicioRepository
                .findById(inventarioServicioId)
                .orElseThrow(() -> new RuntimeException("Asignacion no encontrada"));

        inventarioServicioDeleteValidator.validate(inventarioServicio);
        inventarioServicioRepository.delete(inventarioServicio);
    }

    public Long obtenerStockPorServicio(String codigoServicio, String codigoItem) {
        Servicio servicio = obtenerServicio(codigoServicio);
        Item item = obtenerItem(codigoItem);

        return inventarioServicioRepository
                .findByServicioIdAndItemId(servicio.getId(), item.getId())
                .map(InventarioServicio::getStockActual)
                .orElse(0L);
    }

    public Long obtenerStockGlobalPorItem(String codigoItem) {
        Item item = obtenerItem(codigoItem);
        // Stock agregado de todos los servicios para un item
        return inventarioServicioRepository.obtenerStockGlobalPorItem(item.getId());
    }

    @Transactional
    public void transferirStockDeSedeAServicio(
            Item item,
            Sede sedeOrigen,
            Servicio servicioDestino,
            Long cantidad
    ) {
        // Transferencia controlada entre inventario de sede e inventario de servicio
        InventarioServicio inventarioServicio = obtenerOcrear(item, servicioDestino);
        InventarioSede inventarioSede = inventarioSedeService.obtenerExistente(item, sedeOrigen);

        inventarioSede.restarStock(cantidad);
        inventarioServicio.sumarStock(cantidad);
    }

    public InventarioServicio obtenerExistente(Item item, Servicio servicio) {
        return inventarioServicioRepository
                .findByServicioIdAndItemId(servicio.getId(), item.getId())
                .orElseThrow(() -> new ValidationException(
                        List.of("El item no esta asignado al servicio")
                ));
    }

    public InventarioServicio obtenerOcrear(Item item, Servicio servicio) {
        return inventarioServicioRepository
                .findByServicioIdAndItemId(servicio.getId(), item.getId())
                .orElseGet(() -> {
                    InventarioServicio nuevo = new InventarioServicio();
                    nuevo.setItem(item);
                    nuevo.setServicio(servicio);
                    nuevo.setStockActual(0L);
                    return inventarioServicioRepository.save(nuevo);
                });
    }

    private Servicio obtenerServicio(String codigoServicio) {
        String codigo = TextNormalizer.normalizeCode(codigoServicio);
        return servicioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ValidationException(List.of("El servicio no existe")));
    }

    private Item obtenerItem(String codigoItem) {
        String codigo = TextNormalizer.normalizeCode(codigoItem);
        return itemRepository.findByCodigoItem(codigo)
                .orElseThrow(() -> new ValidationException(List.of("El item no existe")));
    }
}
