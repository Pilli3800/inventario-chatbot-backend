package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoInventarioSearchRequest;
import com.pilli3800.inventario.data.models.MovimientoInventario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioSpecifications {

    public static Specification<MovimientoInventario> search(MovimientoInventarioSearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // Tipo de movimiento
            if (request.tipoMovimiento() != null) {
                predicates.add(
                        cb.equal(
                                root.get("tipoMovimiento"),
                                request.tipoMovimiento()
                        )
                );
            }

            // Usuario
            if (request.usuario() != null && !request.usuario().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("usuario").get("identUsuario")),
                                "%" + request.usuario().toLowerCase() + "%"
                        )
                );
            }

            // Item (desde inventario origen o destino)
            if (request.codigoItem() != null && !request.codigoItem().isBlank()) {

                Join<Object, Object> invOrigen =
                        root.join("inventarioOrigen", JoinType.LEFT);
                Join<Object, Object> invDestino =
                        root.join("inventarioDestino", JoinType.LEFT);

                Predicate origenItem = cb.equal(
                        invOrigen.get("item").get("codigoItem"),
                        request.codigoItem()
                );

                Predicate destinoItem = cb.equal(
                        invDestino.get("item").get("codigoItem"),
                        request.codigoItem()
                );

                predicates.add(cb.or(origenItem, destinoItem));
            }

            // Sede (origen o destino)
            if (request.sedeCodigo() != null && !request.sedeCodigo().isBlank()) {

                Join<Object, Object> invOrigen =
                        root.join("inventarioOrigen", JoinType.LEFT);
                Join<Object, Object> invDestino =
                        root.join("inventarioDestino", JoinType.LEFT);

                Predicate origenSede = cb.equal(
                        invOrigen.get("sede").get("codigo"),
                        request.sedeCodigo()
                );

                Predicate destinoSede = cb.equal(
                        invDestino.get("sede").get("codigo"),
                        request.sedeCodigo()
                );

                predicates.add(cb.or(origenSede, destinoSede));
            }

            // Rango de fechas
            if (request.fechaDesde() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("fechaMovimiento"),
                                request.fechaDesde().atStartOfDay()
                        )
                );
            }

            if (request.fechaHasta() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("fechaMovimiento"),
                                request.fechaHasta().atTime(23, 59, 59)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}