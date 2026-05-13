package com.pilli3800.inventario.data.dto.response.ia;

import java.util.List;

public record ChatHistoryResponse(
        String sessionId,
        List<ChatHistoryMessageResponse> messages
) {
}
