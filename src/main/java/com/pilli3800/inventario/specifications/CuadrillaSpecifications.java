package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaSearchRequest;
import com.pilli3800.inventario.data.models.Cuadrilla;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CuadrillaSpecifications {

    public static Specification<Cuadrilla> search(CuadrillaSearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // Código cuadrilla
            if (request.codigoCuadrilla() != null && !request.codigoCuadrilla().isBlank()) {
                predicates.add(
                        cb.equal(root.get("codigoCuadrilla"), request.codigoCuadrilla())
                );
            }

            // Estado
            if (request.enabled() != null) {
                predicates.add(
                        cb.equal(root.get("enabled"), request.enabled())
                );
            }

            // Jefe de cuadrilla por identUsuario
            if (request.identUsuarioJefe() != null && !request.identUsuarioJefe().isBlank()) {

                Join<Object, Object> jefe = root.join("jefeCuadrilla");

                predicates.add(
                        cb.equal(
                                jefe.get("identUsuario"),
                                request.identUsuarioJefe()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}