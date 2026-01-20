package com.pilli3800.inventario.data.models.user;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ADMINISTRACION, LOGISTICA, GERENCIA, JEFE_CUADRILLA
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombreRol;

}
