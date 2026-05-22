package com.pilli3800.inventario.data.models.conteofisico;

import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.enums.TipoInventarioConteo;
import com.pilli3800.inventario.data.models.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "conteos_fisicos")
@Data
public class ConteoFisico extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conteo_fisico")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inventario", nullable = false, length = 20)
    private TipoInventarioConteo tipoInventario;

    @ManyToOne
    @JoinColumn(name = "sede_id")
    private Sede sede;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "fecha_conteo", nullable = false)
    private LocalDateTime fechaConteo;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(
            mappedBy = "conteo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ConteoFisicoDetalle> detalles = new ArrayList<>();
}
