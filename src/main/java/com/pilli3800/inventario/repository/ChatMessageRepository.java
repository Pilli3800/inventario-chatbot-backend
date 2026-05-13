package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.chat.ChatMessage;
import com.pilli3800.inventario.data.models.chat.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop20BySessionOrderByIdDesc(ChatSession session);

    List<ChatMessage> findBySessionOrderByIdAsc(ChatSession session);
}
