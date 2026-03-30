package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long>, JpaSpecificationExecutor<Proveedor> {

    Optional<Proveedor> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByRuc(String ruc);

    Optional<Proveedor> findByRuc(String ruc);

    Optional<Proveedor> findTopByOrderByIdDesc();
}
