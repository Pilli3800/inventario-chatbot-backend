package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.auth.AuthResponse;
import com.pilli3800.inventario.data.dto.request.LoginRequest;
import com.pilli3800.inventario.data.dto.request.RegisterRequest;
import com.pilli3800.inventario.data.models.user.Role;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.repository.RoleRepository;
import com.pilli3800.inventario.repository.UserRepository;
import com.pilli3800.inventario.security.jwt.JwtService;
import com.pilli3800.inventario.validator.RegisterValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RegisterValidator registerValidator;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.identUsuario(),
                        request.password()
                )
        );

        User user = userRepository
                .findByIdentUsuario(request.identUsuario())
                .orElseThrow();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getIdentUsuario(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombreRol()))
                        .toList()
        );

        String token = jwtService.generateToken(userDetails, user.getIdentUsuario());

        return new AuthResponse("Bearer " + token);
    }

    public AuthResponse register(RegisterRequest request) {
        registerValidator.validate(request);

        Set<Role> roles = request.roles().stream().map(
                nombre -> roleRepository.findByNombreRol(nombre)
                        .orElseThrow(() -> new RuntimeException("Rol no existe: " + nombre)))
                .collect(Collectors.toSet());

        User user = new User();
        user.setIdentUsuario(request.identUsuario());
        user.setDni(request.dni());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNombres(request.nombres());
        user.setApellidos(request.apellidos());
        user.setRoles(roles);

        userRepository.save(user);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getIdentUsuario(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombreRol()))
                        .toList()
        );

        String token = jwtService.generateToken(userDetails, user.getIdentUsuario());

        return new AuthResponse("Bearer " + token);
    }

    @Transactional
    public void cambiarPassword(String identUsuario, String actualPassword, String nuevaPassword) {
        User user = userRepository.findByIdentUsuario(identUsuario)
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(actualPassword, user.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(nuevaPassword));

        userRepository.save(user);
    }
}