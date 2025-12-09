package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdentUsuario(String identUsuario);

    boolean existsByEmail(String email);

    boolean existsByIdentUsuario(String identUsuario);

    boolean existsByDni(String dni);
}
