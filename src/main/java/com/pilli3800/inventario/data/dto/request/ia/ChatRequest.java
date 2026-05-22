package com.pilli3800.inventario.data.dto.request.ia;

public record ChatRequest(
        String message,
        String sessionId,
        ChatScreenContextRequest contextoPantalla
) {
}
