package com.pilli3800.inventario.data.models;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.enums.TipoMovimiento;
import com.pilli3800.inventario.data.models.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "movimientos_inventario")
@Data
public class MovimientoInventario extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Long cantidad;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "inventario_origen_id")
    private InventarioSede inventarioOrigen;

    @ManyToOne
    @JoinColumn(name = "inventario_destino_id")
    private InventarioSede inventarioDestino;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "cuadrilla_id")
    private Cuadrilla cuadrilla;
}
