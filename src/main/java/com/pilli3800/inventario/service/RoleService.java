package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.response.general.CodigoDescriptionDto;
import com.pilli3800.inventario.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<CodigoDescriptionDto> getRoles() {
        return roleRepository.findAll().stream().map(role -> new CodigoDescriptionDto(
                role.getId(),
                role.getNombreRol()
        )).toList();
    }
}
