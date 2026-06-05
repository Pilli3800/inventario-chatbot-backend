package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.ia.ChatScreenContextRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatPromptService {

    private static final String SYSTEM_PROMPT_BASE = """
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

    public String obtenerPromptBase() {
        return SYSTEM_PROMPT_BASE;
    }

    public String construirPromptSistema(
            ChatScreenContextRequest contextoPantalla,
            Collection<? extends GrantedAuthority> roles
    ) {
        if (contextoPantalla == null) {
            return SYSTEM_PROMPT_BASE
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "Roles del usuario autenticado: " + unirRoles(roles);
        }

        return SYSTEM_PROMPT_BASE
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
