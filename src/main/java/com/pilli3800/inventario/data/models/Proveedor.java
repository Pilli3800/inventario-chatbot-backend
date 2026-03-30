package com.pilli3800.inventario.data.models;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(
        name = "proveedores",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"codigo"}),
                @UniqueConstraint(columnNames = {"ruc"})
        }
)
@Data
public class Proveedor extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;

    @Column(nullable = false, length = 8, unique = true)
    private String codigo;

    @Column(nullable = false, length = 11, unique = true)
    private String ruc;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 150)
    private String email;

    @Column(length = 9)
    private String telefono;

    @Column(length = 255)
    private String observaciones;

    @Column(nullable = false)
    private boolean enabled = true;
}
