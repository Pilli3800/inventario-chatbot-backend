package com.pilli3800.inventario.data.models.valesalida;

import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vale_salida_detalle")
@Data
public class ValeSalidaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vale_salida_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vale_salida_id", nullable = false)
    private ValeSalida valeSalida;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;

    @Column(name = "cantidad_entregada", nullable = false)
    private Long cantidadEntregada;

    @Column(name = "codigo_item_snapshot", nullable = false, length = 50)
    private String codigoItemSnapshot;

    @Column(name = "nombre_item_snapshot", nullable = false, length = 150)
    private String nombreItemSnapshot;

    @Column(name = "descripcion_item_snapshot", length = 500)
    private String descripcionItemSnapshot;

    @Column(name = "orden_trabajo", length = 100)
    private String ordenTrabajo;

    @Column(name = "estado_observacion", length = 250)
    private String estadoObservacion;
}
