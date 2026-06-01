package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.response.ia.ChatResponse;
import com.pilli3800.inventario.data.dto.request.ia.ChatScreenContextRequest;
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

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            Tambien puedes brindar soporte de uso sobre la pantalla actual si el frontend envia contextoPantalla.
            Para soporte de pantalla, usa la tool de soporte al usuario y responde como una persona de soporte: breve, claro y natural.
            No conviertas la respuesta de soporte en una guia larga, manual, tabla ni checklist salvo que el usuario lo pida.
            Primero explica para que sirve la pantalla y el flujo principal en pocas lineas.
            Luego ofrece ayudar con una duda concreta de esa pantalla.
            No puedes crear, modificar, aprobar, eliminar ni ejecutar acciones por el usuario.
            No inventes botones, campos ni permisos que no aparezcan en el contexto de pantalla.
            Ten en cuenta los roles del usuario autenticado para no sugerir opciones que no correspondan a su perfil.
            """;

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
                    .system(construirPromptSistema(contextoPantalla, roles))
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

    private String construirPromptSistema(
            ChatScreenContextRequest contextoPantalla,
            Collection<? extends GrantedAuthority> roles
    ) {
        if (contextoPantalla == null) {
            return SYSTEM_PROMPT
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "Roles del usuario autenticado: " + unirRoles(roles);
        }

        return SYSTEM_PROMPT
                + System.lineSeparator()
                + System.lineSeparator()
                + "Roles del usuario autenticado: " + unirRoles(roles)
                + System.lineSeparator()
                + System.lineSeparator()
                + "Contexto de pantalla actual enviado por el frontend:"
                + System.lineSeparator()
                + "- Ruta: " + valor(contextoPantalla.ruta())
                + System.lineSeparator()
                + "- Titulo: " + valor(contextoPantalla.titulo())
                + System.lineSeparator()
                + "- Modulo: " + valor(contextoPantalla.modulo())
                + System.lineSeparator()
                + "- Elementos visibles: " + unir(contextoPantalla.elementosVisibles())
                + System.lineSeparator()
                + "- Acciones disponibles: " + unir(contextoPantalla.accionesDisponibles())
                + System.lineSeparator()
                + "Si el usuario pide ayuda sobre la pantalla actual, usa este contexto y la tool obtenerAyudaPantallaActual.";
    }

    private String unirRoles(Collection<? extends GrantedAuthority> roles) {
        if (roles == null || roles.isEmpty()) {
            return "sin roles";
        }

        return roles.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(rol -> rol != null && !rol.isBlank())
                .map(rol -> rol.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
    }

    private String unir(List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return "sin datos";
        }

        return valores.stream()
                .filter(valor -> valor != null && !valor.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private String valor(String valor) {
        return valor == null || valor.isBlank()
                ? "sin dato"
                : valor.trim();
    }
}
