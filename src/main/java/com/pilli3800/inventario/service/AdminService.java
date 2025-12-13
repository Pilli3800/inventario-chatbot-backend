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
}