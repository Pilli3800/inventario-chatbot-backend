package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardFechaDto;
import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardResumenDto;
import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardTopUsuarioDto;
import com.pilli3800.inventario.data.models.chat.IaChatMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IaChatMetricaRepository extends JpaRepository<IaChatMetrica, Long> {

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardResumenDto(
        COUNT(m.id),
        SUM(CASE WHEN m.exitosa = true THEN 1 ELSE 0 END),
        SUM(CASE WHEN m.exitosa = false THEN 1 ELSE 0 END),
        COUNT(DISTINCT m.sessionId),
        COUNT(DISTINCT m.usuario)
    )
    FROM IaChatMetrica m
    WHERE m.fechaConsulta >= :fechaDesde
      AND m.fechaConsulta <= :fechaHasta
      AND (:usuario IS NULL OR m.usuario = :usuario)
""")
    IaChatDashboardResumenDto obtenerResumenDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario
    );

    @Query("""
    SELECT COUNT(m.id)
    FROM IaChatMetrica m
    WHERE m.fechaConsulta >= :fechaDesde
      AND m.fechaConsulta <= :fechaHasta
      AND (:usuario IS NULL OR m.usuario = :usuario)
""")
    Long contarConsultas(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario
    );

    @Query("""
    SELECT AVG(m.tiempoRespuestaMs)
    FROM IaChatMetrica m
    WHERE m.fechaConsulta >= :fechaDesde
      AND m.fechaConsulta <= :fechaHasta
      AND m.exitosa = true
      AND m.tiempoRespuestaMs IS NOT NULL
      AND (:usuario IS NULL OR m.usuario = :usuario)
""")
    Double obtenerPromedioTiempoRespuestaMs(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario
    );

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardFechaDto(
        CAST(m.fechaConsulta AS localdate),
        COUNT(m.id),
        COUNT(DISTINCT m.sessionId),
        COUNT(DISTINCT m.usuario)
    )
    FROM IaChatMetrica m
    WHERE m.fechaConsulta >= :fechaDesde
      AND m.fechaConsulta <= :fechaHasta
      AND (:usuario IS NULL OR m.usuario = :usuario)
    GROUP BY CAST(m.fechaConsulta AS localdate)
    ORDER BY CAST(m.fechaConsulta AS localdate)
""")
    List<IaChatDashboardFechaDto> obtenerPorFechaDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario
    );

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardTopUsuarioDto(
        m.usuario,
        COUNT(m.id),
        COUNT(DISTINCT m.sessionId)
    )
    FROM IaChatMetrica m
    WHERE m.fechaConsulta >= :fechaDesde
      AND m.fechaConsulta <= :fechaHasta
      AND (:usuario IS NULL OR m.usuario = :usuario)
    GROUP BY m.usuario
    ORDER BY COUNT(m.id) DESC
""")
    List<IaChatDashboardTopUsuarioDto> obtenerTopUsuariosDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario
    );
}
