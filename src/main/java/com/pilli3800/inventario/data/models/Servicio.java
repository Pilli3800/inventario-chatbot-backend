package com.pilli3800.inventario.data.models;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "servicios")
@Data
public class Servicio extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @Column(unique = true, nullable = false,  length = 8)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private boolean enabled = true;

}