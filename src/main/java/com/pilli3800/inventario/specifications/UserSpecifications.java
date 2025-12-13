package com.pilli3800.inventario.specifications;

import com.pilli3800.inventario.data.dto.request.UserSearchRequest;
import com.pilli3800.inventario.data.models.user.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {

    public static Specification<User> search(UserSearchRequest request) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            query.distinct(true);

            if (request.nombres() != null && !request.nombres().isBlank()) {

                String nombres = request.nombres()
                        .toLowerCase()
                        .replaceAll("\\s+", "");

                predicates.add(
                        cb.like(
                                cb.function(
                                        "replace",
                                        String.class,
                                        cb.lower(root.get("nombres")),
                                        cb.literal(" "),
                                        cb.literal("")
                                ),
                                "%" + nombres + "%"
                        )
                );
            }

            if (request.apellidos() != null && !request.apellidos().isBlank()) {

                String apellidos = request.apellidos()
                        .toLowerCase()
                        .replaceAll("\\s+", "");

                predicates.add(
                        cb.like(
                                cb.function(
                                        "replace",
                                        String.class,
                                        cb.lower(root.get("apellidos")),
                                        cb.literal(" "),
                                        cb.literal("")
                                ),
                                "%" + apellidos + "%"
                        )
                );
            }

            if (request.email() != null) {
                predicates.add(
                        cb.equal(root.get("email"), request.email())
                );
            }

            if (request.identUsuario() != null) {
                predicates.add(
                        cb.equal(root.get("identUsuario"), request.identUsuario())
                );
            }

            if (request.dni() != null) {
                predicates.add(
                        cb.equal(root.get("dni"), request.dni())
                );
            }

            if (request.enabled() != null) {
                predicates.add(
                        cb.equal(root.get("enabled"), request.enabled())
                );
            }

            if (request.rol() != null && !request.rol().isBlank()) {
                predicates.add(
                        cb.equal(
                                root.join("roles").get("nombreRol"),
                                request.rol()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
