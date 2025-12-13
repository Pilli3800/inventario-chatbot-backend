package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.user.Role;
import com.pilli3800.inventario.data.models.user.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserDto(
        Long id,
        String identUsuario,
        String dni,
        String email,
        String nombres,
        String apellidos,
        Boolean enabled,
        Set<String> roles
) {

    public static UserDto from(User user) {
        return new UserDto(
           user.getId(),
           user.getIdentUsuario(),
           user.getDni(),
           user.getEmail(),
           user.getNombres(),
           user.getApellidos(),
           user.isEnabled(),
           user.getRoles().stream().map(Role::getNombreRol).collect(Collectors.toSet())
        );
    }
}
