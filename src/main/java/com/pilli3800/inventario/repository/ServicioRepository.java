package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long>, JpaSpecificationExecutor<Servicio> {

    Optional<Servicio> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}