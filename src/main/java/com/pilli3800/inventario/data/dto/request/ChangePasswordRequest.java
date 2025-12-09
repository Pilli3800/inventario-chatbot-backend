package com.pilli3800.inventario.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank
        String actualPassword,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,20}$",
                message = "Debe tener entre 8 y 20 caracteres, incluir mayúscula, minúscula, número y al menos uno de los siguientes símbolos: @ $ ! % * ? &"
        )
        String nuevaPassword
) {}
