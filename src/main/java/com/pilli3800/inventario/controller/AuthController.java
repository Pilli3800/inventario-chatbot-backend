package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.auth.AuthResponse;
import com.pilli3800.inventario.data.dto.request.ChangePasswordRequest;
import com.pilli3800.inventario.data.dto.request.LoginRequest;
import com.pilli3800.inventario.data.dto.request.RegisterRequest;
import com.pilli3800.inventario.data.dto.response.SingleResponse;
import com.pilli3800.inventario.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public SingleResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return new SingleResponse<>(
                200,
                "/api/auth/login",
                authService.login(request));
    }

    @PostMapping("/register")
    public SingleResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new SingleResponse<>(
                200,
                "/api/auth/register",
                authService.register(request)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/cambiar-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        String identUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.cambiarPassword(identUsuario, req.actualPassword(), req.nuevaPassword());
    }
}