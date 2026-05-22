package com.pilli3800.inventario.data.models.comprobantedevolucion;

import com.pilli3800.inventario.data.models.item.Item;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "comprobante_devolucion_detalle")
@Data
public class ComprobanteDevolucionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante_devolucion_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comprobante_devolucion_id", nullable = false)
    private ComprobanteDevolucion comprobanteDevolucion;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;

    @Column(name = "cantidad_devuelta", nullable = false)
    private Long cantidadDevuelta;

    @Column(name = "codigo_item_snapshot", nullable = false, length = 50)
    private String codigoItemSnapshot;

    @Column(name = "nombre_item_snapshot", nullable = false, length = 150)
    private String nombreItemSnapshot;

    @Column(name = "descripcion_item_snapshot", length = 500)
    private String descripcionItemSnapshot;

    @Column(name = "estado_observacion", length = 250)
    private String estadoObservacion;
}
