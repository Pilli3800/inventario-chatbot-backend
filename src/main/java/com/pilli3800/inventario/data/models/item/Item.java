package com.pilli3800.inventario.data.models.item;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.enums.TipoItem;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "items")
@Data
public class Item extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoItem tipo;

    @Column(nullable = false, length = 150)
    private String nombre;

    private String descripcion;

    @Column(name = "codigo_item", unique = true, nullable = false, length = 50)
    private String codigoItem;

    @Column(name = "stock_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal stockTotal = BigDecimal.ZERO;;

    @Column(name = "stock_disponible", nullable = false, precision = 12, scale = 2)
    private BigDecimal stockDisponible = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean enabled = true;

    private String observaciones;
}
