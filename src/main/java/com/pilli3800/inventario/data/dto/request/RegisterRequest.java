package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record RegisterRequest(
        @NotBlank
        String identUsuario,

        @NotBlank
        @Pattern(regexp="\\d{8}", message="debe tener 8 dígitos numéricos")
        String dni,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,20}$",
                message = "Debe tener entre 8 y 20 caracteres, incluir mayúscula, minúscula, número y al menos uno de los siguientes símbolos: @ $ ! % * ? &"
        )
        String password,

        @NotBlank
        String nombres,

        @NotBlank
        String apellidos,

        @NotNull
        Set<String> roles
) {}

