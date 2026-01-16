package com.pilli3800.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;

    public String getIdentUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName().equals("anonymousUser")) {
            return null;
        }

        return auth.getName();
    }

    public List<String> getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority) // Extraemos el nombre del rol
                    .collect(Collectors.toList());
        }
        return Collections.emptyList(); // Si no hay autenticación, devolvemos una lista vacía
    }

    public boolean isAdmin() {
        List<String> roles = getUserRoles();
        return roles.contains("ROLE_ADMINISTRACION");
    }
}
