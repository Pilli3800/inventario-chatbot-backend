package com.pilli3800.inventario.data.models.solicituditems;

import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.Servicio;
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
    @JoinColumn(name = "servicio_origen_id", nullable = false)
    private Servicio servicioOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitudItems estado;

    @Column(length = 500)
    private String observaciones;

    @Column(length = 500)
    private String observacionesAprobacion;

    @Column(length = 500)
    private String observacionesEntrega;

    @Column(length = 500)
    private String observacionesDevolucion;

    @Column(length = 500)
    private String observacionesCierre;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne
    @JoinColumn(name = "usuario_aprobacion_id")
    private User usuarioAprobacion;

    @ManyToOne
    @JoinColumn(name = "usuario_rechazo_id")
    private User usuarioRechazo;

    @ManyToOne
    @JoinColumn(name = "usuario_entrega_id")
    private User usuarioEntrega;

    @ManyToOne
    @JoinColumn(name = "usuario_devolucion_id")
    private User usuarioDevolucion;

    @ManyToOne
    @JoinColumn(name = "usuario_cierre_id")
    private User usuarioCierre;

    @OneToMany(
            mappedBy = "solicitud",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SolicitudItemsDetalle> detalles = new ArrayList<>();
}
