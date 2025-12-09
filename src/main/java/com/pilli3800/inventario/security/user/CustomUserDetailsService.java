package com.pilli3800.inventario.security.user;

import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // username = identUsuario
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository
                .findByIdentUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombreRol()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getIdentUsuario(),
                user.getPassword(),
                user.isEnabled(),
                true,      // accountNonExpired
                true,      // credentialsNonExpired
                true,      // accountNonLocked
                authorities
        );
    }
}