package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.RegisterRequest;
import com.pilli3800.inventario.data.dto.request.ResetPasswordRequest;
import com.pilli3800.inventario.data.dto.request.UserSearchRequest;
import com.pilli3800.inventario.data.dto.request.UserUpdateRequest;
import com.pilli3800.inventario.data.dto.response.UserDto;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMINISTRACION')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<UserDto> createUser(@Valid @RequestBody RegisterRequest request) {
        return new SingleResponse<>(
                201,
                "/api/admin/users",
                adminService.createUser(request)
        );
    }

    @PutMapping("/{identUsuario}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<UserDto> updateUser(
            @PathVariable String identUsuario,
            @RequestBody UserUpdateRequest request
    ) {
        return new SingleResponse<>(
                200,
                "/api/admin/users/" + identUsuario,
                adminService.updateUser(identUsuario, request)
        );
    }

    @PatchMapping("/{identUsuario}/desactivar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> disableUser(@PathVariable String identUsuario) {
        adminService.disableUser(identUsuario);
        return new SingleResponse<>(200, "/api/admin/users/" + identUsuario + "/desactivar", "Usuario desactivado");
    }

    @PatchMapping("/{identUsuario}/activar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> enableUser(@PathVariable String identUsuario) {
        adminService.enableUser(identUsuario);
        return new SingleResponse<>(200, "/api/admin/users/" + identUsuario + "/activar", "Usuario activado");
    }

    @PatchMapping("/{identUsuario}/password")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> resetPassword(
            @PathVariable String identUsuario,
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        adminService.resetPassword(identUsuario, request.password());
        return new SingleResponse<>(
                200,
                "/api/admin/users/" + identUsuario + "/password",
                "Contraseña actualizada por administrador"
        );
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportUsersExcel(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String identUsuario,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String rol
    ) throws IOException {

        UserSearchRequest request = new UserSearchRequest(
                nombres, apellidos, email, identUsuario, dni, enabled, rol
        );

        byte[] excel = adminService.exportUsersToExcel(request);

        String fecha = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "usuarios_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}