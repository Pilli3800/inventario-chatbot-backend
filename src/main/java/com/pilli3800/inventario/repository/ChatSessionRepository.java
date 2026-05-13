package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.chat.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);

    @Query("""
            select s
            from ChatSession s
            where coalesce(s.fcActualizacion, s.fcCreacion) < :fechaLimite
            """)
    List<ChatSession> findSessionsToDelete(LocalDateTime fechaLimite);
}
