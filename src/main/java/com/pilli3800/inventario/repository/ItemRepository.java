package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.dto.response.ItemDto;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByCodigoItem(String codigoItem);

    boolean existsByCodigoItem(String codigoItem);

    List<Item> findTop5ByNombreContainingIgnoreCase(String nombreItem);

    @Query("""
        SELECT m
        FROM MovimientoInventario m
        LEFT JOIN m.inventarioOrigen io
        LEFT JOIN m.inventarioDestino idst
        WHERE (io.item.codigoItem = :codigoItem OR idst.item.codigoItem = :codigoItem)
        ORDER BY m.fechaMovimiento DESC
    """)
    List<MovimientoInventario> findHistorialMovimientosPorCodigoItem(
            @Param("codigoItem") String codigoItem
    );
}
