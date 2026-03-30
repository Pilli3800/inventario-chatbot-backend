package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioServicioRepository extends JpaRepository<InventarioServicio, Long>, JpaSpecificationExecutor<InventarioServicio> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventarioServicio> findByServicioIdAndItemId(Long servicioId, Long itemId);

    boolean existsByItemAndServicio(Item item, Servicio servicio);

    @Query("""
        SELECT COALESCE(SUM(i.stockActual), 0)
        FROM InventarioServicio i
        WHERE i.item.id = :itemId
    """)
    Long obtenerStockGlobalPorItem(@Param("itemId") Long itemId);
}
