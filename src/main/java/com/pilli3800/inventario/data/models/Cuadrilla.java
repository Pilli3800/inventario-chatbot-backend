package com.pilli3800.inventario.data.models;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "cuadrillas")
@Data
public class Cuadrilla extends RegistroAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuadrilla")
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String codigoCuadrilla;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jefe_cuadrilla_id", nullable = false)
    private User jefeCuadrilla;

    private boolean enabled = true;
}
