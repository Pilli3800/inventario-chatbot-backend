package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.ItemSearchRequest;
import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ItemSpecifications {

    public static Specification<Item> search(ItemSearchRequest request) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            // Nombre
            if (request.nombre() != null && !request.nombre().isBlank()) {

                String nombre = request.nombre()
                        .toLowerCase()
                        .replaceAll("\\s+", "");

                predicates.add(
                        cb.like(
                                cb.function(
                                        "replace",
                                        String.class,
                                        cb.lower(root.get("nombre")),
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
                                        cb.lower(root.get("codigoItem")),
                                        cb.literal(" "),
                                        cb.literal("")
                                ),
                                "%" + codigo + "%"
                        )
                );
            }

            // TipoItem
            if (request.tipo() != null) {
                predicates.add(
                        cb.equal(root.get("tipo"), request.tipo())
                );
            }

            // Estado
            if (request.enabled() != null) {
                predicates.add(
                        cb.equal(root.get("enabled"), request.enabled())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Item> searchByTextoInicial(String textoInicial, Boolean enabled) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (textoInicial != null && !textoInicial.isBlank()) {

                String texto = textoInicial
                        .toLowerCase()
                        .replaceAll("\\s+", "");

                predicates.add(
                        cb.like(
                                cb.function(
                                        "replace",
                                        String.class,
                                        cb.lower(root.get("nombre")),
                                        cb.literal(" "),
                                        cb.literal("")
                                ),
                                texto + "%"
                        )
                );
            }

            if (enabled != null) {
                predicates.add(
                        cb.equal(root.get("enabled"), enabled)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
