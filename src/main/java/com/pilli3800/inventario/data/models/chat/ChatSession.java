package com.pilli3800.inventario.data.models.chat;

import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoria;
import com.pilli3800.inventario.data.models.auditoria.RegistroAuditoriaListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(RegistroAuditoriaListener.class)
@Table(
        name = "chat_sessions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id"})
        }
)
@Data
public class ChatSession extends RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chat_session")
    private Long id;

    @Column(name = "session_id", nullable = false, length = 36, unique = true)
    private String sessionId;

    @Column(name = "usuario", nullable = false, length = 150)
    private String usuario;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();
}
