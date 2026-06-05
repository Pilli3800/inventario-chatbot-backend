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

    @Column(name = "codigo_item", unique = true, nullable = false, length = 50)
    private String codigoItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoItem tipo;

    @Column(nullable = false, length = 150)
    private String nombre;

    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "stock_minimo")
    private Long stockMinimo;

    private String observaciones;
}
