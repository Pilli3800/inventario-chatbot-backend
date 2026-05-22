package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.response.ia.ChatResponse;
import com.pilli3800.inventario.data.models.chat.ChatMessage;
import com.pilli3800.inventario.data.models.chat.ChatSession;
import com.pilli3800.inventario.data.models.enums.ChatMessageRole;
import com.pilli3800.inventario.tool.chat.CapacidadesTools;
import com.pilli3800.inventario.tool.consumo.ConsumoTools;
import com.pilli3800.inventario.tool.inventario.InventarioStockTools;
import com.pilli3800.inventario.tool.item.ItemTools;
import com.pilli3800.inventario.tool.movimiento.MovimientoTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private static final String SYSTEM_PROMPT = """
            Eres un asistente interno del sistema de inventario.
            Usa solo la informacion devuelta por las tools disponibles.
            Si una tool devuelve una URL o enlace, debes incluirlo en la respuesta final exactamente como fue devuelto, sin omitirlo ni resumirlo.
            Si la tool entrega un detalle de movimiento, prioriza responder con esa informacion y conserva el enlace si existe.
            Si una tool devuelve una tabla en Markdown, debes conservarla en formato Markdown y no convertirla en texto narrativo.
            Quiero que uses emojis en formato markdown donde creas que es necesario para hacer sentir al usuario que eres un agente inteligente.
            """;

    private final ChatClient chatClient;
    private final ItemTools itemTools;
    private final MovimientoTools movimientoTools;
    private final ConsumoTools consumoTools;
    private final CapacidadesTools capacidadesTools;
    private final InventarioStockTools inventarioStockTools;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryService chatMemoryService;
    private final IaChatMetricaService iaChatMetricaService;

    public ChatResponse chat(String message, String sessionId, String usuario) {
        String sessionIdMetrica = sessionId;

        try {
            ChatSession session = chatSessionService.getOrCreateSession(sessionId, usuario);
            sessionIdMetrica = session.getSessionId();

            List<Message> messages = new ArrayList<>();
            for (ChatMessage chatMessage : chatMemoryService.getRecentMessages(session)) {
                if (chatMessage.getRole() == ChatMessageRole.USER) {
                    messages.add(new UserMessage(chatMessage.getContent()));
                    continue;
                }

                messages.add(new AssistantMessage(chatMessage.getContent()));
            }

            messages.add(new UserMessage(message));

            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .messages(messages)
                    .toolCallbacks(ToolCallbacks.from(
                            itemTools,
                            movimientoTools,
                            consumoTools,
                            capacidadesTools,
                            inventarioStockTools
                    ))
                    .call()
                    .content();

            chatMemoryService.saveInteraction(session, message, response);
            registrarMetrica(sessionIdMetrica, usuario, message, response, true, null);

            return new ChatResponse(session.getSessionId(), response);
        } catch (RuntimeException e) {
            registrarMetrica(sessionIdMetrica, usuario, message, null, false, e.getMessage());
            throw e;
        }
    }

    private void registrarMetrica(
            String sessionId,
            String usuario,
            String mensaje,
            String respuesta,
            boolean exitosa,
            String mensajeError
    ) {
        try {
            iaChatMetricaService.registrarConsulta(
                    sessionId,
                    usuario,
                    mensaje,
                    respuesta,
                    exitosa,
                    mensajeError
            );
        } catch (RuntimeException ignored) {
        }
    }
}
