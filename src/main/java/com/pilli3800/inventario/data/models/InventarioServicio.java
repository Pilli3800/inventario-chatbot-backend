package com.pilli3800.inventario.data.models;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(
        name = "inventarios_servicio",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"servicio_id", "item_id"})
        }
)
@Data
public class InventarioServicio extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "stock_actual", nullable = false)
    private Long stockActual = 0L;

    public void sumarStock(Long cantidad) {
        this.stockActual += cantidad;
    }

    public void restarStock(Long cantidad) {
        if (this.stockActual < cantidad) {
            throw new IllegalStateException("Stock insuficiente");
        }
        this.stockActual -= cantidad;
    }
}
