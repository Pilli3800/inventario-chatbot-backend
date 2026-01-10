package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.UserSearchRequest;
import com.pilli3800.inventario.data.dto.response.UserDto;
import com.pilli3800.inventario.data.dto.response.general.PageResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AdminService adminService;

    @GetMapping("/{identUsuario}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<UserDto> getUser(@PathVariable String  identUsuario) {
        return new SingleResponse<>(
                200,
                "/users/{identUsuario}",
                adminService.getUser(identUsuario)
        );
    }

    @GetMapping
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
}