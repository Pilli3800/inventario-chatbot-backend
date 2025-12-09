package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNombreRol(String name);

    boolean existsByNombreRol(String nombre);
}
