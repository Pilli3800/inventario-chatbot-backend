package com.pilli3800.inventario.data.models.comprobantedevolucion;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(
        name = "comprobante_devolucion",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"solicitud_id"})
        }
)
@Data
public class ComprobanteDevolucion extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante_devolucion")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudItems solicitud;

    @Column(name = "numero_comprobante", nullable = false, unique = true, length = 30)
    private String numeroComprobante;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(
            mappedBy = "comprobanteDevolucion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ComprobanteDevolucionDetalle> detalles = new ArrayList<>();

    @OneToMany(
            mappedBy = "comprobanteDevolucion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ComprobanteDevolucionFirma> firmas = new ArrayList<>();
}
