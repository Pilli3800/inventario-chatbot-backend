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

            query.distinct(true);

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
            Join<Object, Object> itemOrigenJoin =
                    invOrigen.join("item", JoinType.LEFT);

            Join<Object, Object> itemDestinoJoin =
                    invDestino.join("item", JoinType.LEFT);
            Join<Object, Object> invServicioOrigen =
                    root.join("inventarioServicioOrigen", JoinType.LEFT);
            Join<Object, Object> invServicioDestino =
                    root.join("inventarioServicioDestino", JoinType.LEFT);
            Join<Object, Object> itemServicioOrigenJoin =
                    invServicioOrigen.join("item", JoinType.LEFT);
            Join<Object, Object> itemServicioDestinoJoin =
                    invServicioDestino.join("item", JoinType.LEFT);

            // ITEM
            if (request.codigoItem() != null && !request.codigoItem().isBlank()) {

                String pattern = "%" + request.codigoItem().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(itemOrigenJoin.get("codigoItem")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(itemDestinoJoin.get("codigoItem")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(itemServicioOrigenJoin.get("codigoItem")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(itemServicioDestinoJoin.get("codigoItem")),
                                        pattern
                                )
                        )
                );
            }

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

            // SERVICIO (a través de cuadrilla)
            if (request.codigoServicio() != null && !request.codigoServicio().isBlank()) {

                Join<Object, Object> cuadrillaJoin =
                        root.join("cuadrilla", JoinType.LEFT);

                Join<Object, Object> servicioJoin =
                        cuadrillaJoin.join("servicio", JoinType.LEFT);

                predicates.add(
                        cb.or(
                                cb.equal(
                                        servicioJoin.get("codigo"),
                                        request.codigoServicio()
                                ),
                                cb.equal(
                                        invServicioOrigen.get("servicio").get("codigo"),
                                        request.codigoServicio()
                                ),
                                cb.equal(
                                        invServicioDestino.get("servicio").get("codigo"),
                                        request.codigoServicio()
                                )
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
