package com.pilli3800.inventario.data.dto.response.ia;

import com.pilli3800.inventario.data.models.chat.ChatMessage;

public record ChatHistoryMessageResponse(
        String role,
        String message
) {

    public static ChatHistoryMessageResponse from(ChatMessage chatMessage) {
        return new ChatHistoryMessageResponse(
                chatMessage.getRole().name().toLowerCase(),
                chatMessage.getContent()
        );
    }
}
