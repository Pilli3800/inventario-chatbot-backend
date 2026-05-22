package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.request.ia.ChatRequest;
import com.pilli3800.inventario.data.dto.response.ia.ChatHistoryResponse;
import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardDto;
import com.pilli3800.inventario.data.dto.response.ia.ChatResponse;
import com.pilli3800.inventario.data.models.chat.ChatSession;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.service.ChatMemoryService;
import com.pilli3800.inventario.service.ChatOrchestrator;
import com.pilli3800.inventario.service.ChatSessionService;
import com.pilli3800.inventario.service.IaChatMetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ia/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatOrchestrator chatOrchestrator;
    private final ChatSessionService chatSessionService;
    private final ChatMemoryService chatMemoryService;
    private final IaChatMetricaService iaChatMetricaService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ChatResponse> chat(
            @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        return new SingleResponse<>(
                200,
                "/api/ia/chat",
                chatOrchestrator.chat(
                        request.message(),
                        request.sessionId(),
                        authentication.getName(),
                        authentication.getAuthorities(),
                        request.contextoPantalla()
                )
        );
    }

    @GetMapping("/{sessionId}/historial")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ChatHistoryResponse> getHistorial(
            @PathVariable String sessionId,
            Authentication authentication
    ) {
        ChatSession session = chatSessionService.getSession(sessionId, authentication.getName());

        return new SingleResponse<>(
                200,
                "/api/ia/chat/" + sessionId + "/historial",
                chatMemoryService.getHistory(session)
        );
    }

    @GetMapping("/dashboard")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<IaChatDashboardDto> getDashboard(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String usuario
    ) {
        return new SingleResponse<>(
                200,
                "/api/ia/chat/dashboard",
                iaChatMetricaService.getDashboard(fechaDesde, fechaHasta, usuario)
        );
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<String> deleteSession(
            @PathVariable String sessionId,
            Authentication authentication
    ) {
        chatSessionService.deleteSession(sessionId, authentication.getName());

        return new SingleResponse<>(
                200,
                "/api/ia/chat/" + sessionId,
                "Sesion eliminada correctamente."
        );
    }
}
