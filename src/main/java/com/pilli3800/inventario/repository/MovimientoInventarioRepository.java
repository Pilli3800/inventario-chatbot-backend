package com.pilli3800.inventario.repository;

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

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long>, JpaSpecificationExecutor<MovimientoInventario> {

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
            (:fecha IS NULL OR DATE(m.fechaMovimiento) = :fecha)
        AND (:fechaDesde IS NULL OR m.fechaMovimiento >= :fechaDesde)
        AND (:fechaHasta IS NULL OR m.fechaMovimiento <= :fechaHasta)
        AND (:mes IS NULL OR FUNCTION('MONTH', m.fechaMovimiento) = :mes)
        AND (:anio IS NULL OR FUNCTION('YEAR', m.fechaMovimiento) = :anio)
        GROUP BY i.codigoItem, i.nombre
        ORDER BY i.codigoItem
    """)
    List<StockMovidoPorItemDto> obtenerStockMovidoPorItem(
            @Param("fecha") LocalDate fecha,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("mes") Integer mes,
            @Param("anio") Integer anio
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
      AND (:fecha IS NULL OR DATE(m.fechaMovimiento) = :fecha)
      AND (:fechaDesde IS NULL OR m.fechaMovimiento >= :fechaDesde)
      AND (:fechaHasta IS NULL OR m.fechaMovimiento <= :fechaHasta)
      AND (:mes IS NULL OR FUNCTION('MONTH', m.fechaMovimiento) = :mes)
      AND (:anio IS NULL OR FUNCTION('YEAR', m.fechaMovimiento) = :anio)
    GROUP BY i.codigoItem, i.nombre
""")
    StockMovidoPorItemDto obtenerStockMovidoPorItem(
            @Param("codigoItem") String codigoItem,
            @Param("fecha") LocalDate fecha,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("mes") Integer mes,
            @Param("anio") Integer anio
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


}
