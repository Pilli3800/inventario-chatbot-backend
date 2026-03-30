package com.pilli3800.inventario.data.models.valesalida;

import com.pilli3800.inventario.data.models.enums.RolValeSalidaFirma;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vale_salida_firma",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"vale_salida_id", "rol_firma"})
        }
)
@Data
public class ValeSalidaFirma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vale_salida_firma")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vale_salida_id", nullable = false)
    private ValeSalida valeSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_firma", nullable = false, length = 30)
    private RolValeSalidaFirma rolFirma;

    @Column(name = "codigo_usuario_snapshot", length = 50)
    private String codigoUsuarioSnapshot;

    @Column(name = "nombre_usuario_snapshot", nullable = false, length = 201)
    private String nombreUsuarioSnapshot;

    @Column(name = "dni_snapshot", nullable = false, length = 8)
    private String dniSnapshot;

    @Column(name = "fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;
}
