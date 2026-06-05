package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.ia.ChatScreenContextRequest;
import com.pilli3800.inventario.data.dto.response.ia.IntentClassificationResult;
import com.pilli3800.inventario.data.models.chat.ChatMessage;
import com.pilli3800.inventario.data.models.enums.ChatIntent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentClassifierService {

    private static final String CLASSIFIER_PROMPT = """
            Clasifica la intencion del mensaje del usuario para un asistente interno de inventario.
            Devuelve solo un objeto estructurado.
            Si tienes duda, devuelve FUERA_DE_CONTEXTO.
            Usa el historial reciente como contexto para resolver continuaciones breves o respuestas incompletas, pero solo si se relacionan claramente con una conversacion de inventario.
            Si el usuario pide crear, modificar, aprobar, eliminar o ejecutar acciones, devuelve FUERA_DE_CONTEXTO.
            Si el usuario saluda o pregunta que puedes hacer, devuelve CAPACIDADES_CHAT.
            Si el usuario pide ayuda sobre la pantalla actual, devuelve AYUDA_PANTALLA.
            Usa BUSCAR_ITEM, CONSULTAR_MOVIMIENTO, CONSULTAR_HISTORIAL, CONSULTAR_STOCK, CONSULTAR_CONSUMO_ANOMALO, CONSULTAR_EVOLUCION_CONSUMO o CONSULTAR_PROYECCION_CONSUMO solo si la consulta pertenece claramente al dominio.
            Extrae codigo de item, nombre de item, sede y servicio cuando existan.
            """;

    private final ChatClient classifierChatClient;

    public IntentClassifierService(
            @Qualifier("classifierChatClient") ChatClient classifierChatClient
    ) {
        this.classifierChatClient = classifierChatClient;
    }

    public IntentClassificationResult classify(
            String message,
            ChatScreenContextRequest contextoPantalla,
            List<ChatMessage> historialReciente
    ) {
        try {
            String userPrompt = construirPromptUsuario(message, contextoPantalla, historialReciente);

            IntentClassificationResult result = classifierChatClient.prompt()
                    .system(CLASSIFIER_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(IntentClassificationResult.class);

            if (result == null || result.intent() == null) {
                return fallback();
            }

            return result;
        } catch (RuntimeException ex) {
            return fallback();
        }
    }

    private String construirPromptUsuario(
            String message,
            ChatScreenContextRequest contextoPantalla,
            List<ChatMessage> historialReciente
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Mensaje del usuario: ").append(valor(message));

        if (contextoPantalla != null) {
            prompt.append(System.lineSeparator())
                    .append("Contexto de pantalla:")
                    .append(System.lineSeparator())
                    .append("- Ruta: ").append(valor(contextoPantalla.ruta()))
                    .append(System.lineSeparator())
                    .append("- Titulo: ").append(valor(contextoPantalla.titulo()))
                    .append(System.lineSeparator())
                    .append("- Modulo: ").append(valor(contextoPantalla.modulo()))
                    .append(System.lineSeparator())
                    .append("- Elementos visibles: ").append(lista(contextoPantalla.elementosVisibles()))
                    .append(System.lineSeparator())
                    .append("- Acciones disponibles: ").append(lista(contextoPantalla.accionesDisponibles()));
        }

        if (historialReciente != null && !historialReciente.isEmpty()) {
            prompt.append(System.lineSeparator())
                    .append("Historial reciente de la conversacion:")
                    .append(System.lineSeparator());

            historialReciente.stream()
                    .skip(Math.max(0, historialReciente.size() - 6L))
                    .forEach(chatMessage -> prompt
                            .append("- ")
                            .append(chatMessage.getRole().name())
                            .append(": ")
                            .append(valor(chatMessage.getContent()))
                            .append(System.lineSeparator()));
        }

        prompt.append(System.lineSeparator())
                .append("Si la intencion no es clara, devuelve FUERA_DE_CONTEXTO.");

        return prompt.toString();
    }

    private IntentClassificationResult fallback() {
        return new IntentClassificationResult(
                ChatIntent.FUERA_DE_CONTEXTO,
                0.0d,
                "Clasificacion no disponible o no confiable",
                null,
                null,
                null,
                null
        );
    }

    private String valor(String valor) {
        return valor == null || valor.isBlank() ? "sin dato" : valor.trim();
    }

    private String lista(java.util.List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return "sin datos";
        }

        return valores.stream()
                .filter(valor -> valor != null && !valor.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + ", " + b)
                .orElse("sin datos");
    }
}
