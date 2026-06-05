package com.pilli3800.inventario.data.dto.response.ia;

import com.pilli3800.inventario.data.models.enums.ChatIntent;

public record IntentClassificationResult(
        ChatIntent intent,
        double confidence,
        String reason,
        String extractedItemCode,
        String extractedItemName,
        String extractedSede,
        String extractedServicio
) {
}
