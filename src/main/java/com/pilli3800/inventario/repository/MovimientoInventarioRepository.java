package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.dto.response.ItemMovimientosCantidadDto;
import com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardFechaTipoDto;
import com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardTipoDto;
import com.pilli3800.inventario.data.dto.response.StockMovidoPorItemDto;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.data.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long>, JpaSpecificationExecutor<MovimientoInventario> {

    @Query("""
        SELECT new com.pilli3800.inventario.data.dto.response.ItemMovimientosCantidadDto(
            i.codigoItem,
            i.nombre,
            COUNT(m.id)
        )
        FROM MovimientoInventario m
        LEFT JOIN m.inventarioOrigen io
        LEFT JOIN m.inventarioDestino id
        LEFT JOIN m.inventarioServicioOrigen iso
        LEFT JOIN m.inventarioServicioDestino isd
        JOIN Item i ON (i = io.item OR i = id.item OR i = iso.item OR i = isd.item)
        WHERE
            m.fechaMovimiento >= :desde
        AND m.fechaMovimiento < :hasta
        GROUP BY i.codigoItem, i.nombre
        ORDER BY COUNT(m.id) DESC, i.codigoItem
    """)
    List<ItemMovimientosCantidadDto> obtenerItemsConMasMovimientos(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    @Query("""
        SELECT new com.pilli3800.inventario.data.dto.response.StockMovidoPorItemDto(
            i.codigoItem,
            i.nombre,
            SUM(m.cantidad)
        )
        FROM MovimientoInventario m
        LEFT JOIN m.inventarioOrigen io
        LEFT JOIN m.inventarioDestino id
        LEFT JOIN m.inventarioServicioOrigen iso
        LEFT JOIN m.inventarioServicioDestino isd
        JOIN Item i ON (i = io.item OR i = id.item OR i = iso.item OR i = isd.item)
        WHERE
            m.fechaMovimiento >= :desde
        AND m.fechaMovimiento < :hasta
        GROUP BY i.codigoItem, i.nombre
        ORDER BY i.codigoItem
    """)
    List<StockMovidoPorItemDto> obtenerStockMovidoPorItem(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.StockMovidoPorItemDto(
        i.codigoItem,
        i.nombre,
        SUM(m.cantidad)
    )
    FROM MovimientoInventario m
    LEFT JOIN m.inventarioOrigen io
    LEFT JOIN m.inventarioDestino id
    LEFT JOIN m.inventarioServicioOrigen iso
    LEFT JOIN m.inventarioServicioDestino isd
    JOIN Item i ON (i = io.item OR i = id.item OR i = iso.item OR i = isd.item)
    WHERE i.codigoItem = :codigoItem
      AND m.fechaMovimiento >= :desde
      AND m.fechaMovimiento < :hasta
    GROUP BY i.codigoItem, i.nombre
""")
    StockMovidoPorItemDto obtenerStockMovidoPorItem(
            @Param("codigoItem") String codigoItem,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    boolean existsByInventarioOrigen(InventarioSede inventarioSede);

    boolean existsByInventarioDestino(InventarioSede inventarioSede);

    boolean existsByInventarioServicioOrigen(InventarioServicio inventarioServicio);

    boolean existsByInventarioServicioDestino(InventarioServicio inventarioServicio);

    @Query("""
    SELECT m
    FROM MovimientoInventario m
    LEFT JOIN m.inventarioOrigen io
    LEFT JOIN m.inventarioDestino id
    LEFT JOIN m.inventarioServicioOrigen iso
    LEFT JOIN m.inventarioServicioDestino isd
    WHERE m.usuario = :usuario
      AND m.cuadrilla = :cuadrilla
      AND (
            io.item = :item
         OR id.item = :item
         OR iso.item = :item
         OR isd.item = :item
      )
    ORDER BY m.fechaMovimiento DESC
""")
    List<MovimientoInventario> findMovimientosPorUsuarioCuadrillaItemOrdenados(
            @Param("usuario") User usuario,
            @Param("cuadrilla") Cuadrilla cuadrilla,
            @Param("item") Item item
    );

    @Query("""
    SELECT m
    FROM MovimientoInventario m
    LEFT JOIN m.inventarioOrigen io
    LEFT JOIN m.inventarioDestino id
    LEFT JOIN m.inventarioServicioOrigen iso
    LEFT JOIN m.inventarioServicioDestino isd
    JOIN Item i ON (i = io.item OR i = id.item OR i = iso.item OR i = isd.item)
    WHERE i = :item
    ORDER BY m.fechaMovimiento DESC
""")
    List<MovimientoInventario> findUltimosMovimientosPorItem(
            @Param("item") Item item
    );

    @Query("""
    SELECT m
    FROM MovimientoInventario m
    LEFT JOIN m.inventarioOrigen io
    LEFT JOIN m.inventarioDestino id
    LEFT JOIN m.inventarioServicioOrigen iso
    LEFT JOIN m.inventarioServicioDestino isd
    JOIN Item i ON (i = io.item OR i = id.item OR i = iso.item OR i = isd.item)
    WHERE i = :item
    ORDER BY m.fechaMovimiento DESC
""")
    List<MovimientoInventario> findHistorialMovimientosPorItem(
            @Param("item") Item item
    );

    @Query("""
    SELECT COALESCE(SUM(m.cantidad), 0)
    FROM MovimientoInventario m
    LEFT JOIN m.inventarioServicioDestino isd
    WHERE m.solicitud.id = :solicitudId
      AND m.tipoMovimiento = com.pilli3800.inventario.data.models.enums.TipoMovimiento.DEVOLUCION
      AND isd.item = :item
""")
    Long sumarCantidadDevueltaPorSolicitudEItem(
            @Param("solicitudId") Long solicitudId,
            @Param("item") Item item
    );

    List<MovimientoInventario> findBySolicitudIdAndTipoMovimientoOrderByIdAsc(
            Long solicitudId,
            com.pilli3800.inventario.data.models.enums.TipoMovimiento tipoMovimiento
    );

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardTipoDto(
        m.tipoMovimiento,
        COUNT(m.id)
    )
    FROM MovimientoInventario m
    LEFT JOIN m.cuadrilla c
    LEFT JOIN c.servicio cs
    LEFT JOIN m.inventarioServicioOrigen iso
    LEFT JOIN iso.servicio isos
    LEFT JOIN m.inventarioServicioDestino isd
    LEFT JOIN isd.servicio isds
    WHERE m.fechaMovimiento >= :fechaDesde
      AND m.fechaMovimiento <= :fechaHasta
      AND m.usuario.identUsuario LIKE :usuario
      AND c.codigoCuadrilla LIKE :codigoCuadrilla
      AND (
            :codigoServicio = '%'
         OR cs.codigo LIKE :codigoServicio
         OR isos.codigo LIKE :codigoServicio
         OR isds.codigo LIKE :codigoServicio
      )
    GROUP BY m.tipoMovimiento
""")
    List<MovimientoHistoricoDashboardTipoDto> contarPorTipoMovimientoDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario,
            @Param("codigoCuadrilla") String codigoCuadrilla,
            @Param("codigoServicio") String codigoServicio
    );

    @Query("""
    SELECT new com.pilli3800.inventario.data.dto.response.MovimientoHistoricoDashboardFechaTipoDto(
        CAST(m.fechaMovimiento AS localdate),
        m.tipoMovimiento,
        COUNT(m.id)
    )
    FROM MovimientoInventario m
    LEFT JOIN m.cuadrilla c
    LEFT JOIN c.servicio cs
    LEFT JOIN m.inventarioServicioOrigen iso
    LEFT JOIN iso.servicio isos
    LEFT JOIN m.inventarioServicioDestino isd
    LEFT JOIN isd.servicio isds
    WHERE m.fechaMovimiento >= :fechaDesde
      AND m.fechaMovimiento <= :fechaHasta
      AND m.usuario.identUsuario LIKE :usuario
      AND c.codigoCuadrilla LIKE :codigoCuadrilla
      AND (
            :codigoServicio = '%'
         OR cs.codigo LIKE :codigoServicio
         OR isos.codigo LIKE :codigoServicio
         OR isds.codigo LIKE :codigoServicio
      )
    GROUP BY CAST(m.fechaMovimiento AS localdate), m.tipoMovimiento
    ORDER BY CAST(m.fechaMovimiento AS localdate) ASC
""")
    List<MovimientoHistoricoDashboardFechaTipoDto> contarPorFechaYTipoMovimientoDashboard(
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("usuario") String usuario,
            @Param("codigoCuadrilla") String codigoCuadrilla,
            @Param("codigoServicio") String codigoServicio
    );

}
