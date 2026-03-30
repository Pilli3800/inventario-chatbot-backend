package com.pilli3800.inventario.data.models.valesalida;

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
        name = "vale_salida",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"solicitud_id"})
        }
)
@Data
public class ValeSalida extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vale_salida")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudItems solicitud;

    @Column(name = "numero_vale", nullable = false, unique = true, length = 30)
    private String numeroVale;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(
            mappedBy = "valeSalida",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ValeSalidaDetalle> detalles = new ArrayList<>();

    @OneToMany(
            mappedBy = "valeSalida",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ValeSalidaFirma> firmas = new ArrayList<>();
}
