package com.pilli3800.inventario.data.models.solicituditems;

import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "solicitud_items_detalle",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"solicitud_id", "item_id"})
        }
)
@Data
public class SolicitudItemsDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_items_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudItems solicitud;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Long cantidad;
}

