package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.FacturaCompraSearchRequest;
import com.pilli3800.inventario.data.models.FacturaCompra;
import com.pilli3800.inventario.data.models.Proveedor;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FacturaCompraSpecifications {

    public static Specification<FacturaCompra> search(FacturaCompraSearchRequest request) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            Join<FacturaCompra, Proveedor> proveedorJoin = root.join("proveedor");

            if (request.codigoProveedor() != null && !request.codigoProveedor().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(proveedorJoin.get("codigo")),
                                "%" + request.codigoProveedor().toLowerCase() + "%"
                        )
                );
            }

            if (request.numeroFactura() != null && !request.numeroFactura().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("numeroFactura")),
                                "%" + request.numeroFactura().toLowerCase() + "%"
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
