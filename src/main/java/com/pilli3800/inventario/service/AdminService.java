package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.RegisterRequest;
import com.pilli3800.inventario.data.dto.request.UserSearchRequest;
import com.pilli3800.inventario.data.dto.request.UserUpdateRequest;
import com.pilli3800.inventario.data.dto.response.UserDto;
import com.pilli3800.inventario.data.models.user.Role;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.repository.RoleRepository;
import com.pilli3800.inventario.repository.UserRepository;
import com.pilli3800.inventario.specifications.UserSpecifications;
import com.pilli3800.inventario.validator.RegisterValidator;
import com.pilli3800.inventario.validator.UserUpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RegisterValidator registerValidator;
    private final UserUpdateValidator userUpdateValidator;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getUser(String identUsuario) {
        return userRepository.findByIdentUsuario(identUsuario)
                .map(UserDto::from).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Page<UserDto> getUsers(UserSearchRequest request, Pageable pageable) {
        Specification<User> spec = UserSpecifications.search(request);
        Page<User> page = userRepository.findAll(spec, pageable);
        return page.map(UserDto::from);
    }

    public UserDto createUser(RegisterRequest request) {
        registerValidator.validate(request);

        User user = new User();
        user.setIdentUsuario(request.identUsuario());
        user.setDni(request.dni());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNombres(request.nombres());
        user.setApellidos(request.apellidos());

        Set<Role> roles = new HashSet<>(roleRepository.findAllByNombreRolIn(request.roles()));
        user.setRoles(roles);

        return UserDto.from(userRepository.save(user));
    }

    public UserDto updateUser(String identUsuario, UserUpdateRequest request) {

        User user = userRepository.findByIdentUsuario(identUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        userUpdateValidator.validate(user.getId(), request);

        user.setIdentUsuario(request.identUsuario());
        user.setDni(request.dni());
        user.setEmail(request.email());
        user.setNombres(request.nombres());
        user.setApellidos(request.apellidos());
        user.setEnabled(request.enabled());

        Set<Role> roles = new HashSet<>(roleRepository.findAllByNombreRolIn(request.roles()));
        user.setRoles(roles);

        return UserDto.from(userRepository.save(user));
    }

    private void setUserEnabled(String identUsuario, boolean enabled) {
        User user = userRepository.findByIdentUsuario(identUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public void enableUser(String identUsuario) {
        setUserEnabled(identUsuario, true);
    }

    public void disableUser(String identUsuario) {
        setUserEnabled(identUsuario, false);
    }

    public void resetPassword(String identUsuario, String nuevaPassword) {
        User user = userRepository.findByIdentUsuario(identUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            throw new RuntimeException("La nueva contraseña no puede estar vacía");
        }

        user.setPassword(passwordEncoder.encode(nuevaPassword));
        userRepository.save(user);
    }

    public byte[] exportUsersToExcel(UserSearchRequest request) throws IOException {

        Specification<User> spec = UserSpecifications.search(request);
        List<User> users = userRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Usuarios");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);

        String[] headers = {
                "ID",
                "Ident Usuario",
                "DNI",
                "Email",
                "Nombres",
                "Apellidos",
                "Activo",
                "Roles"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getIdentUsuario());
            row.createCell(2).setCellValue(user.getDni());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getNombres());
            row.createCell(5).setCellValue(user.getApellidos());
            row.createCell(6).setCellValue(user.isEnabled());

            String roles = user.getRoles()
                    .stream()
                    .map(Role::getNombreRol)
                    .reduce((r1, r2) -> r1 + ", " + r2)
                    .orElse("");

            row.createCell(7).setCellValue(roles);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

}