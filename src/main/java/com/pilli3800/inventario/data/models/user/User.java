package com.pilli3800.inventario.data.models.user;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "users")
@Data
public class User extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificador de negocio: USER1345
    @Column(name = "ident_usuario", nullable = false, unique = true, length = 50)
    private String identUsuario;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
