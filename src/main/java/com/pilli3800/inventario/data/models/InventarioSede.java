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
        name = "inventario_sedes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"item_id", "sede_id"})
        }
)
@Data
public class InventarioSede extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario_sede")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Column(nullable = false)
    private Long stock = 0L;

    public void sumarStock(Long cantidad) {
        this.stock += cantidad;
    }

    public void restarStock(Long cantidad) {
        if (this.stock < cantidad) {
            throw new IllegalStateException("Stock insuficiente");
        }
        this.stock -= cantidad;
    }
}