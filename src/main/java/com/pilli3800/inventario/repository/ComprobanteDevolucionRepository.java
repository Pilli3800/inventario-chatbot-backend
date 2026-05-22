package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteDevolucionRepository extends JpaRepository<ComprobanteDevolucion, Long> {

    boolean existsBySolicitudId(Long solicitudId);

    Optional<ComprobanteDevolucion> findBySolicitudId(Long solicitudId);
}
