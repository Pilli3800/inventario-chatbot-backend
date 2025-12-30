package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByCodigoItem(String codigoItem);

    boolean existsByCodigoItem(String codigoItem);
}
