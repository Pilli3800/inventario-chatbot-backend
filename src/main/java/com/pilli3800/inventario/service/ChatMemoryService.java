package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.response.ia.ChatHistoryMessageResponse;
import com.pilli3800.inventario.data.dto.response.ia.ChatHistoryResponse;
import com.pilli3800.inventario.data.models.chat.ChatMessage;
import com.pilli3800.inventario.data.models.chat.ChatSession;
import com.pilli3800.inventario.data.models.enums.ChatMessageRole;
import com.pilli3800.inventario.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getRecentMessages(ChatSession session) {
        List<ChatMessage> messages = chatMessageRepository.findTop20BySessionOrderByIdDesc(session);
        Collections.reverse(messages);
        return messages;
    }

    public ChatHistoryResponse getHistory(ChatSession session) {
        List<ChatHistoryMessageResponse> messages = chatMessageRepository.findBySessionOrderByIdAsc(session)
                .stream()
                .map(ChatHistoryMessageResponse::from)
                .toList();

        return new ChatHistoryResponse(session.getSessionId(), messages);
    }

    public void saveInteraction(ChatSession session, String userMessage, String assistantMessage) {
        ChatMessage user = new ChatMessage();
        user.setSession(session);
        user.setRole(ChatMessageRole.USER);
        user.setContent(userMessage);
        chatMessageRepository.save(user);

        ChatMessage assistant = new ChatMessage();
        assistant.setSession(session);
        assistant.setRole(ChatMessageRole.ASSISTANT);
        assistant.setContent(assistantMessage);
        chatMessageRepository.save(assistant);
    }
}
