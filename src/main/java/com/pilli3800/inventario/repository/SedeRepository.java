package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.dto.response.SedeDto;
import com.pilli3800.inventario.data.models.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SedeRepository extends JpaRepository<Sede, Long> {

    Optional<Sede> findByCodigo(String codigoSede);

    List<Sede> findAllByEnabledTrue();
}
