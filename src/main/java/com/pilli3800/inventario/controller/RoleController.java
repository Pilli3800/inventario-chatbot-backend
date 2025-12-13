package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.response.general.CodigoDescriptionDto;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMINISTRACION')")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<List<CodigoDescriptionDto>> getRoles() {
        return new SingleResponse<>(
                200,
                "/api/admin/roles",
                roleService.getRoles()
        );
    }
}
