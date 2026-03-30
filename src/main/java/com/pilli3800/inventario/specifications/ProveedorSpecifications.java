package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.ProveedorSearchRequest;
import com.pilli3800.inventario.data.models.Proveedor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProveedorSpecifications {

    public static Specification<Proveedor> search(ProveedorSearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (request.codigo() != null && !request.codigo().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("codigo")),
                                "%" + request.codigo().toLowerCase() + "%"
                        )
                );
            }

            if (request.ruc() != null && !request.ruc().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("ruc")),
                                "%" + request.ruc().toLowerCase() + "%"
                        )
                );
            }

            if (request.nombre() != null && !request.nombre().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("nombre")),
                                "%" + request.nombre().toLowerCase() + "%"
                        )
                );
            }

            if (request.enabled() != null) {
                predicates.add(
                        cb.equal(root.get("enabled"), request.enabled())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
