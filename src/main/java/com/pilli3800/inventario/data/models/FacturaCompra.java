package com.pilli3800.inventario.data.models;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(
        name = "facturas_compra",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"proveedor_id", "numero_factura"}),
                @UniqueConstraint(columnNames = {"proveedor_id", "serie", "correlativo"})
        }
)
@Data
public class FacturaCompra extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura_compra")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", nullable = false, length = 50)
    private String numeroFactura;

    @Column(length = 4)
    private String serie;

    @Column(length = 8)
    private String correlativo;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(length = 500)
    private String observaciones;

    // Campo reservado para futura integracion de XML/PDF
    @Column(name = "documento_referencia", length = 255)
    private String documentoReferencia;
}
