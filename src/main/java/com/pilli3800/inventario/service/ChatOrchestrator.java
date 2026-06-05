package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.ia.ChatScreenContextRequest;
import com.pilli3800.inventario.data.dto.response.ia.ChatResponse;
import com.pilli3800.inventario.data.models.chat.ChatMessage;
import com.pilli3800.inventario.data.models.chat.ChatSession;
import com.pilli3800.inventario.data.models.enums.ChatMessageRole;
import com.pilli3800.inventario.tool.chat.CapacidadesTools;
import com.pilli3800.inventario.tool.chat.SoporteUsuarioTools;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final ChatClient chatClient;
    private final ItemTools itemTools;
    private final MovimientoTools movimientoTools;
    private final ConsumoTools consumoTools;
    private final CapacidadesTools capacidadesTools;
    private final SoporteUsuarioTools soporteUsuarioTools;
    private final InventarioStockTools inventarioStockTools;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryService chatMemoryService;
    private final IaChatMetricaService iaChatMetricaService;
    private final DomainGuardService domainGuardService;
    private final ChatPromptService chatPromptService;

    public ChatResponse chat(
            String message,
            String sessionId,
            String usuario,
            Collection<? extends GrantedAuthority> roles,
            ChatScreenContextRequest contextoPantalla
    ) {
        long inicioMs = System.currentTimeMillis();
        String sessionIdMetrica = sessionId;

        try {
            ChatSession session = chatSessionService.getOrCreateSession(sessionId, usuario);
            sessionIdMetrica = session.getSessionId();
            List<ChatMessage> recentMessages = chatMemoryService.getRecentMessages(session);

            String respuestaBloqueada = domainGuardService.evaluar(
                    message,
                    session.getSessionId(),
                    contextoPantalla,
                    recentMessages
            );
            if (respuestaBloqueada != null) {
                chatMemoryService.saveInteraction(session, message, respuestaBloqueada);
                registrarMetrica(
                        sessionIdMetrica,
                        usuario,
                        message,
                        respuestaBloqueada,
                        true,
                        null,
                        calcularTiempoRespuestaMs(inicioMs)
                );

                return new ChatResponse(session.getSessionId(), respuestaBloqueada);
            }

            List<Message> messages = new ArrayList<>();
            for (ChatMessage chatMessage : recentMessages) {
                if (chatMessage.getRole() == ChatMessageRole.USER) {
                    messages.add(new UserMessage(chatMessage.getContent()));
                    continue;
                }

                messages.add(new AssistantMessage(chatMessage.getContent()));
            }

            messages.add(new UserMessage(message));

            String response = chatClient.prompt()
                    .system(chatPromptService.construirPromptSistema(contextoPantalla, roles))
                    .messages(messages)
                    .toolCallbacks(ToolCallbacks.from(
                            itemTools,
                            movimientoTools,
                            consumoTools,
                            capacidadesTools,
                            soporteUsuarioTools,
                            inventarioStockTools
                    ))
                    .call()
                    .content();

            chatMemoryService.saveInteraction(session, message, response);
            registrarMetrica(sessionIdMetrica, usuario, message, response, true, null, calcularTiempoRespuestaMs(inicioMs));

            return new ChatResponse(session.getSessionId(), response);
        } catch (RuntimeException e) {
            registrarMetrica(sessionIdMetrica, usuario, message, null, false, e.getMessage(), calcularTiempoRespuestaMs(inicioMs));
            throw e;
        }
    }

    private void registrarMetrica(
            String sessionId,
            String usuario,
            String mensaje,
            String respuesta,
            boolean exitosa,
            String mensajeError,
            Long tiempoRespuestaMs
    ) {
        try {
            iaChatMetricaService.registrarConsulta(
                    sessionId,
                    usuario,
                    mensaje,
                    respuesta,
                    exitosa,
                    mensajeError,
                    tiempoRespuestaMs
            );
        } catch (RuntimeException ignored) {
        }
    }

    private Long calcularTiempoRespuestaMs(long inicioMs) {
        return System.currentTimeMillis() - inicioMs;
    }
}
