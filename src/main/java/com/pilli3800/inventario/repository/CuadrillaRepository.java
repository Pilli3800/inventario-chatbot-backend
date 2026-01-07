package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.Cuadrilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuadrillaRepository extends JpaRepository<Cuadrilla, Long>, JpaSpecificationExecutor<Cuadrilla> {

    Optional<Cuadrilla> findByCodigoCuadrilla(String codigoCuadrilla);

    boolean existsByCodigoCuadrilla(String codigoCuadrilla);
}
