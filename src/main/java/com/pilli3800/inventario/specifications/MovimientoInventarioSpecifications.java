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

    public static Specification<MovimientoInventario> search(
            MovimientoInventarioSearchRequest request
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Tipo movimiento
            if (request.tipoMovimiento() != null) {
                predicates.add(
                        cb.equal(root.get("tipoMovimiento"), request.tipoMovimiento())
                );
            }

            // Usuario
            if (request.usuario() != null && !request.usuario().isBlank()) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("usuario").get("identUsuario")),
                                request.usuario().toLowerCase()
                        )
                );
            }

            Join<Object, Object> invOrigen =
                    root.join("inventarioOrigen", JoinType.LEFT);
            Join<Object, Object> invDestino =
                    root.join("inventarioDestino", JoinType.LEFT);

            // SEDE ORIGEN
            if (request.sedeOrigen() != null && !request.sedeOrigen().isBlank()) {
                predicates.add(
                        cb.equal(
                                invOrigen.get("sede").get("codigo"),
                                request.sedeOrigen()
                        )
                );
            }

            // SEDE DESTINO
            if (request.sedeDestino() != null && !request.sedeDestino().isBlank()) {
                predicates.add(
                        cb.equal(
                                invDestino.get("sede").get("codigo"),
                                request.sedeDestino()
                        )
                );
            }

            // Fechas
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

            // Cuadrilla (solo auditoría)
            if (request.codigoCuadrilla() != null && !request.codigoCuadrilla().isBlank()) {
                Join<Object, Object> cuadrillaJoin =
                        root.join("cuadrilla", JoinType.LEFT);

                predicates.add(
                        cb.equal(
                                cuadrillaJoin.get("codigoCuadrilla"),
                                request.codigoCuadrilla()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<MovimientoInventario> byCuadrillaId(Long cuadrillaId) {
        return (root, query, cb) ->
                cb.equal(root.get("cuadrilla").get("id"), cuadrillaId);
    }
}