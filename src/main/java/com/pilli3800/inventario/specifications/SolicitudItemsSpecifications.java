package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.solicituditems.SolicitudItemsSearchRequest;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SolicitudItemsSpecifications {

    public static Specification<SolicitudItems> search(
            SolicitudItemsSearchRequest request
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (request.codigoCuadrilla() != null && !request.codigoCuadrilla().isBlank()) {
                predicates.add(
                        cb.equal(
                                root.get("cuadrilla").get("codigoCuadrilla"),
                                request.codigoCuadrilla()
                        )
                );
            }

            if (request.solicitanteId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("solicitante").get("id"),
                                request.solicitanteId()
                        )
                );
            } else if (request.identUsuario() != null && !request.identUsuario().isBlank()) {
                predicates.add(
                        cb.equal(
                                root.get("solicitante").get("identUsuario"),
                                request.identUsuario()
                        )
                );
            }

            if (request.sedeOrigenCodigo() != null && !request.sedeOrigenCodigo().isBlank()) {
                predicates.add(
                        cb.equal(
                                root.get("sedeOrigen").get("codigo"),
                                request.sedeOrigenCodigo()
                        )
                );
            }

            if (request.estado() != null) {
                predicates.add(
                        cb.equal(root.get("estado"), request.estado())
                );
            }

            if (request.fechaDesde() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("fcCreacion"),
                                request.fechaDesde().atStartOfDay()
                        )
                );
            }

            if (request.fechaHasta() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("fcCreacion"),
                                request.fechaHasta().atTime(23, 59, 59)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
