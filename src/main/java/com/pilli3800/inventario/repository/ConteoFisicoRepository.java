package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.dto.response.ConteoFisicoDashboardResumenDto;
import com.pilli3800.inventario.data.models.conteofisico.ConteoFisico;
import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ConteoFisicoRepository
        extends JpaRepository<ConteoFisico, Long>, JpaSpecificationExecutor<ConteoFisico> {

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.ConteoFisicoDashboardResumenDto(
        COUNT(DISTINCT c.id),
        COUNT(d.id),
        SUM(CASE WHEN d.stockSistema > 0 THEN 1 ELSE 0 END),
        SUM(CASE WHEN d.stockSistema > 0 AND d.diferencia <> 0 THEN 1 ELSE 0 END),
        SUM(CASE WHEN d.stockSistema > 0 THEN d.stockSistema ELSE 0 END),
        SUM(CASE WHEN d.stockSistema > 0 THEN d.cantidadFisica ELSE 0 END),
        SUM(CASE WHEN d.stockSistema > 0 THEN ABS(d.diferencia) ELSE 0 END)
    )
    FROM ConteoFisico c
    JOIN c.detalles d
    LEFT JOIN c.sede s
    LEFT JOIN c.servicio sv
    WHERE c.fechaConteo >= :fechaDesde
      AND c.fechaConteo <= :fechaHasta
      AND (:usuarioId IS NULL OR c.usuario.id = :usuarioId)
      AND (:usuario IS NULL OR c.usuario.identUsuario = :usuario)
      AND (:tipoInventario IS NULL OR c.tipoInventario = :tipoInventario)
      AND (
            :codigoUbicacion IS NULL
         OR s.codigo = :codigoUbicacion
         OR sv.codigo = :codigoUbicacion
      )
""")
    ConteoFisicoDashboardResumenDto obtenerResumenDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuarioId") Long usuarioId,
            @Param("usuario") String usuario,
            @Param("tipoInventario") TipoInventarioConteo tipoInventario,
            @Param("codigoUbicacion") String codigoUbicacion
    );
}
