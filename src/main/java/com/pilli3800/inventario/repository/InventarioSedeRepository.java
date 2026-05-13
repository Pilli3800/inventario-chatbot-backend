package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioSedeRepository extends JpaRepository<InventarioSede, Long>, JpaSpecificationExecutor<InventarioSede> {

    boolean existsByItemAndSede(Item item, Sede sede);

    Optional<InventarioSede> findByItemIdAndSedeId(Long itemId, Long sedeId);

}
