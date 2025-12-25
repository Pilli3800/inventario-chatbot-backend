package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.RegisterRequest;
import com.pilli3800.inventario.data.dto.request.ResetPasswordRequest;
import com.pilli3800.inventario.data.dto.request.UserSearchRequest;
import com.pilli3800.inventario.data.dto.request.UserUpdateRequest;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.dto.response.UserDto;
import com.pilli3800.inventario.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRACION')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping("/users/{identUsuario}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<UserDto> getUser(@PathVariable String  identUsuario) {
        return new SingleResponse<>(
                200,
                "/users/{identUsuario}",
                adminService.getUser(identUsuario)
        );
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<UserDto> search(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String identUsuario,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String rol,
            @PageableDefault(page = 0, size = 5, sort = "nombres") Pageable pageable
    ) {
        UserSearchRequest request = new UserSearchRequest(
                nombres, apellidos, email, identUsuario, dni, enabled, rol
        );

        Page<UserDto> page = adminService.getUsers(request, pageable);

        return PageResponse.from(page);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponse<UserDto> createUser(@Valid @RequestBody RegisterRequest request) {
        return new SingleResponse<>(
                201,
                "/api/admin/users",
                adminService.createUser(request)
        );
    }

    @PutMapping("/users/{identUsuario}")
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

    @PatchMapping("/users/{identUsuario}/desactivar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> disableUser(@PathVariable String identUsuario) {
        adminService.disableUser(identUsuario);
        return new SingleResponse<>(200, "/api/admin/users/" + identUsuario + "/desactivar", "Usuario desactivado");
    }

    @PatchMapping("/users/{identUsuario}/activar")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> enableUser(@PathVariable String identUsuario) {
        adminService.enableUser(identUsuario);
        return new SingleResponse<>(200, "/api/admin/users/" + identUsuario + "/activar", "Usuario activado");
    }

    @PatchMapping("/users/{identUsuario}/password")
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
}
