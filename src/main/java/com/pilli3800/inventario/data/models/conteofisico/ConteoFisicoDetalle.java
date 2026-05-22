package com.pilli3800.inventario.data.models.conteofisico;

import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "conteos_fisicos_detalle",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"conteo_id", "item_id"})
        }
)
@Data
public class ConteoFisicoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conteo_fisico_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conteo_id", nullable = false)
    private ConteoFisico conteo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "stock_sistema", nullable = false)
    private Long stockSistema;

    @Column(name = "cantidad_fisica", nullable = false)
    private Long cantidadFisica;

    @Column(nullable = false)
    private Long diferencia;

    @Column(length = 500)
    private String observacion;
}
