package com.pilli3800.inventario.tool.chat;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class CapacidadesTools {

    @Tool(description = "Explica que puede hacer el asistente. Usala cuando el usuario pregunte que puedes hacer, como funciona el chat, en que puedes ayudar o que consultas soporta el sistema.")
    @PreAuthorize("isAuthenticated()")
    public String getCapacidadesDelChat() {
        return """
                Puedo ayudarte solo con consultas internas soportadas por las tools del sistema.
                Actualmente puedo:
                - Buscar items activos por nombre o descripcion aproximada.
                - Buscar items activos cuyo nombre empiece con un texto especifico.
                - Buscar items por codigo exacto o parcial.
                - Buscar items activos por tipo: MATERIAL, HERRAMIENTA o EQUIPO.
                - Consultar consumos anómalos por cuadrilla e item.
                - Consultar la evolución del consumo de un item en una cuadrilla.
                - Consultar una proyección simple de consumo futuro por item.
                - Mantener el contexto de la conversacion dentro de la sesion actual.
                No tengo permiso para ayudarte con consultas fuera del sistema o fuera de las tools disponibles.
                """;
    }
}
