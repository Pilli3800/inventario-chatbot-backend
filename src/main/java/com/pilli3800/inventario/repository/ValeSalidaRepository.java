package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.valesalida.ValeSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValeSalidaRepository extends JpaRepository<ValeSalida, Long> {

    boolean existsBySolicitudId(Long solicitudId);

    Optional<ValeSalida> findById(Long id);

    Optional<ValeSalida> findBySolicitudId(Long solicitudId);
}
