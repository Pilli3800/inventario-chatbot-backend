package com.pilli3800.inventario.data.models.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class RegistroAuditoria {
    @Column(name = "fc_creacion", updatable = false)
    private LocalDateTime fcCreacion;

    @Column(name = "usu_creacion", updatable = false)
    private String usuCreacion;

    @Column(name = "fc_actualizacion")
    private LocalDateTime fcActualizacion;

    @Column(name = "usu_actualizacion")
    private String usuActualizacion;
}
