package com.pilli3800.inventario.data.models.chat;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(name = "ia_chat_metricas")
@Data
public class IaChatMetrica extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ia_chat_metrica")
    private Long id;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "usuario", nullable = false, length = 150)
    private String usuario;

    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    @Column(name = "longitud_mensaje", nullable = false)
    private Integer longitudMensaje;

    @Column(name = "longitud_respuesta", nullable = false)
    private Integer longitudRespuesta;

    @Column(name = "exitosa", nullable = false)
    private boolean exitosa;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;
}
