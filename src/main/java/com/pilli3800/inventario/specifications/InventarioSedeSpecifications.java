package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.InventarioSedeSearchRequest;
import com.pilli3800.inventario.data.models.InventarioSede;
import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InventarioSedeSpecifications {

    public static Specification<InventarioSede> search(InventarioSedeSearchRequest request) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // Sede (obligatoria)
            predicates.add(
                    cb.equal(root.get("sede").get("codigo"), request.sedeCodigo())
            );

            Join<InventarioSede, Item> itemJoin = root.join("item");

            // Nombre Item
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

            // Código Item
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

            // Tipo Item
            if (request.tipoItem() != null) {
                predicates.add(
                        cb.equal(itemJoin.get("tipo"), request.tipoItem())
                );
            }

            // Estado Item
            if (request.enabledItem() != null) {
                predicates.add(
                        cb.equal(itemJoin.get("enabled"), request.enabledItem())
                );
            }

            // Con stock / sin stock
            if (request.conStock() != null) {
                if (request.conStock()) {
                    predicates.add(cb.greaterThan(root.get("stock"), 0L));
                } else {
                    predicates.add(cb.equal(root.get("stock"), 0L));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}