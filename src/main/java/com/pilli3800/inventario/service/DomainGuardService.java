package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.ia.ChatScreenContextRequest;
import com.pilli3800.inventario.data.dto.response.ia.IntentClassificationResult;
import com.pilli3800.inventario.data.models.chat.ChatMessage;
import com.pilli3800.inventario.data.models.enums.ChatIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainGuardService {

    private static final double MIN_CONFIDENCE = 0.50d;
    private static final String BLOCKED_RESPONSE = "\uD83D\uDE05 Ups! No estoy preparado para ayudarte con eso. ";
    private final IntentClassifierService intentClassifierService;

    public String evaluar(
            String message,
            String sessionId,
            ChatScreenContextRequest contextoPantalla,
            List<ChatMessage> historialReciente
    ) {
        IntentClassificationResult classification = intentClassifierService.classify(
                message,
                contextoPantalla,
                historialReciente
        );

        if (classification == null || classification.intent() == null) {
            return BLOCKED_RESPONSE;
        }

        if (classification.intent() == ChatIntent.FUERA_DE_CONTEXTO) {
            return BLOCKED_RESPONSE;
        }

        if (classification.confidence() < MIN_CONFIDENCE) {
            return BLOCKED_RESPONSE;
        }

        return null;
    }
}
