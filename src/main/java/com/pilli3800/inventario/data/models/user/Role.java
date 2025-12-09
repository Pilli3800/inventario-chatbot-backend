package com.pilli3800.inventario.data.models.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ADMINISTRACIÓN, LOGÍSTICA, GERENCIA, USUARIO, JEFE_CUADRILLA
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombreRol;

}
