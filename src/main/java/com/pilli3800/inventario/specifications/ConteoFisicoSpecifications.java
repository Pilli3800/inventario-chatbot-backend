package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.conteofisico.ConteoFisicoSearchRequest;
import com.pilli3800.inventario.data.models.conteofisico.ConteoFisico;
import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ConteoFisicoSpecifications {

    public static Specification<ConteoFisico> search(ConteoFisicoSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (request.fechaDesde() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("fechaConteo"),
                                request.fechaDesde().atStartOfDay()
                        )
                );
            }

            if (request.fechaHasta() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("fechaConteo"),
                                request.fechaHasta().atTime(23, 59, 59)
                        )
                );
            }

            if (request.usuarioId() != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), request.usuarioId()));
            } else if (request.usuario() != null && !request.usuario().isBlank()) {
                predicates.add(cb.equal(root.get("usuario").get("identUsuario"), request.usuario()));
            }

            if (request.tipoInventario() != null) {
                predicates.add(cb.equal(root.get("tipoInventario"), request.tipoInventario()));
            }

            if (request.codigoUbicacion() != null && !request.codigoUbicacion().isBlank()) {
                if (request.tipoInventario() == TipoInventarioConteo.SEDE) {
                    predicates.add(cb.equal(root.join("sede", JoinType.LEFT).get("codigo"), request.codigoUbicacion()));
                } else if (request.tipoInventario() == TipoInventarioConteo.SERVICIO) {
                    predicates.add(cb.equal(root.join("servicio", JoinType.LEFT).get("codigo"), request.codigoUbicacion()));
                } else {
                    predicates.add(
                            cb.or(
                                    cb.equal(root.join("sede", JoinType.LEFT).get("codigo"), request.codigoUbicacion()),
                                    cb.equal(root.join("servicio", JoinType.LEFT).get("codigo"), request.codigoUbicacion())
                            )
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
