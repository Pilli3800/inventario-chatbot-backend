package com.pilli3800.inventario.data.models.comprobantedevolucion;

import com.pilli3800.inventario.data.models.enums.RolComprobanteDevolucionFirma;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "comprobante_devolucion_firma",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"comprobante_devolucion_id", "rol_firma"})
        }
)
@Data
public class ComprobanteDevolucionFirma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante_devolucion_firma")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comprobante_devolucion_id", nullable = false)
    private ComprobanteDevolucion comprobanteDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_firma", nullable = false, length = 30)
    private RolComprobanteDevolucionFirma rolFirma;

    @Column(name = "codigo_usuario_snapshot", length = 50)
    private String codigoUsuarioSnapshot;

    @Column(name = "nombre_usuario_snapshot", nullable = false, length = 201)
    private String nombreUsuarioSnapshot;

    @Column(name = "dni_snapshot", nullable = false, length = 8)
    private String dniSnapshot;

    @Column(name = "fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;
}
