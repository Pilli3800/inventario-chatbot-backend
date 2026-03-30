package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.FacturaCompra;
import com.pilli3800.inventario.data.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaCompraRepository extends JpaRepository<FacturaCompra, Long>, JpaSpecificationExecutor<FacturaCompra> {

    Optional<FacturaCompra> findByProveedorAndNumeroFactura(Proveedor proveedor, String numeroFactura);

    boolean existsByProveedorAndNumeroFactura(Proveedor proveedor, String numeroFactura);

    Optional<FacturaCompra> findByProveedorAndSerieAndCorrelativo(
            Proveedor proveedor,
            String serie,
            String correlativo
    );

    boolean existsByProveedorAndSerieAndCorrelativo(
            Proveedor proveedor,
            String serie,
            String correlativo
    );
}
