package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.models.chat.ChatSession;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;

    @Value("${inventario.chat.session-retention-days}")
    private int sessionRetentionDays;

    public ChatSession getOrCreateSession(String sessionId, String usuario) {
        if (sessionId == null || sessionId.isBlank()) {
            return crearSession(usuario);
        }

        ChatSession session = getSession(sessionId, usuario);
        return touchSession(session);
    }

    public ChatSession getSession(String sessionId, String usuario) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ValidationException(List.of("La sesion de chat no existe.")));

        if (!session.getUsuario().equals(usuario)) {
            throw new ValidationException(List.of("La sesion de chat no pertenece al usuario autenticado."));
        }

        return session;
    }

    public void deleteSession(String sessionId, String usuario) {
        ChatSession session = getSession(sessionId, usuario);
        chatSessionRepository.delete(session);
    }

    private ChatSession crearSession(String usuario) {
        ChatSession session = new ChatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUsuario(usuario);
        return chatSessionRepository.save(session);
    }

    private ChatSession touchSession(ChatSession session) {
        session.setFcActualizacion(LocalDateTime.now());
        return chatSessionRepository.save(session);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredSessions() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(sessionRetentionDays);
        List<ChatSession> sessions = chatSessionRepository.findSessionsToDelete(fechaLimite);

        if (sessions.isEmpty()) {
            return;
        }

        chatSessionRepository.deleteAll(sessions);
    }
}
