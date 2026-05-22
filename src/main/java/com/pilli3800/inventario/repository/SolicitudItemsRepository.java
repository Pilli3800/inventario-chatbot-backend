package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import com.pilli3800.inventario.data.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface SolicitudItemsRepository
        extends JpaRepository<SolicitudItems, Long>, JpaSpecificationExecutor<SolicitudItems> {

    boolean existsBySolicitanteAndEstadoIn(
            User solicitante,
            Collection<EstadoSolicitudItems> estados
    );

    @Query("""
    SELECT s
    FROM SolicitudItems s
    WHERE s.fcCreacion >= :fechaDesde
      AND s.fcCreacion <= :fechaHasta
      AND (:servicioOrigenCodigo IS NULL OR s.servicioOrigen.codigo = :servicioOrigenCodigo)
      AND (:codigoCuadrilla IS NULL OR s.cuadrilla.codigoCuadrilla = :codigoCuadrilla)
      AND (:solicitanteId IS NULL OR s.solicitante.id = :solicitanteId)
    ORDER BY s.id DESC
""")
    List<SolicitudItems> buscarParaDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("servicioOrigenCodigo") String servicioOrigenCodigo,
            @Param("codigoCuadrilla") String codigoCuadrilla,
            @Param("solicitanteId") Long solicitanteId
    );
}

