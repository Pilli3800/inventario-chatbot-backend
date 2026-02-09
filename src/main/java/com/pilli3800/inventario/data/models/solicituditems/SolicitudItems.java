package com.pilli3800.inventario.data.models.solicituditems;

import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
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
@Table(name = "solicitud_items")
@Data
public class SolicitudItems extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_items")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cuadrilla_id", nullable = false)
    private Cuadrilla cuadrilla;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private User solicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_origen_id", nullable = false)
    private Sede sedeOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudItems estado;

    @Column(length = 500)
    private String observaciones;

    @Column(length = 500)
    private String observacionesAprobacion;

    @Column(length = 500)
    private String observacionesEntrega;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @ManyToOne
    @JoinColumn(name = "usuario_aprobacion_id")
    private User usuarioAprobacion;

    @ManyToOne
    @JoinColumn(name = "usuario_rechazo_id")
    private User usuarioRechazo;

    @ManyToOne
    @JoinColumn(name = "usuario_entrega_id")
    private User usuarioEntrega;

    @OneToMany(
            mappedBy = "solicitud",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SolicitudItemsDetalle> detalles = new ArrayList<>();
}
