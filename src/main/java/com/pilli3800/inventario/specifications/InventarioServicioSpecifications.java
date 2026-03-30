package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.InventarioServicioSearchRequest;
import com.pilli3800.inventario.data.models.InventarioServicio;
import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InventarioServicioSpecifications {

    public static Specification<InventarioServicio> search(InventarioServicioSearchRequest request) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.equal(root.get("servicio").get("codigo"), request.codigoServicio())
            );

            Join<InventarioServicio, Item> itemJoin = root.join("item");

            if (request.nombreItem() != null && !request.nombreItem().isBlank()) {
                String nombre = request.nombreItem()
                        .toLowerCase()
                        .replaceAll("\\s+", "");

                predicates.add(
                        cb.like(
                                cb.function(
                                        "replace",
                                        String.class,
                                        cb.lower(itemJoin.get("nombre")),
                                        cb.literal(" "),
                                        cb.literal("")
                                ),
                                "%" + nombre + "%"
                        )
                );
            }

            if (request.codigoItem() != null && !request.codigoItem().isBlank()) {
                String codigo = request.codigoItem()
                        .toLowerCase()
                        .replaceAll("\\s+", "");

                predicates.add(
                        cb.like(
                                cb.function(
                                        "replace",
                                        String.class,
                                        cb.lower(itemJoin.get("codigoItem")),
                                        cb.literal(" "),
                                        cb.literal("")
                                ),
                                "%" + codigo + "%"
                        )
                );
            }

            if (request.tipoItem() != null) {
                predicates.add(
                        cb.equal(itemJoin.get("tipo"), request.tipoItem())
                );
            }

            if (request.enabledItem() != null) {
                predicates.add(
                        cb.equal(itemJoin.get("enabled"), request.enabledItem())
                );
            }

            if (request.conStock() != null) {
                if (request.conStock()) {
                    predicates.add(cb.greaterThan(root.get("stockActual"), 0L));
                } else {
                    predicates.add(cb.equal(root.get("stockActual"), 0L));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
