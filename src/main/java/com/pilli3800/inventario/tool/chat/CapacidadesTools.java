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
                Puedo:
                - Buscar items activos por nombre, descripcion aproximada, codigo o tipo. Necesito que me digas el nombre, un codigo, una parte del texto o el tipo que quieras revisar.
                - Buscar items por letra inicial. Necesito la letra o el texto con el que quieres empezar.
                - Consultar el ultimo movimiento de un item. Necesito el codigo del item.
                - Consultar el historial reciente de movimientos de un item. Necesito el codigo del item.
                - Consultar stock de un item en sede o servicio. Necesito el codigo del item y decirme si quieres revisar una sede o un servicio, junto con su codigo.
                - Listar items criticos por sede. Necesito el codigo de la sede.
                - Listar items criticos por servicio. Necesito el codigo del servicio.
                - Revisar consumos anomalos para ver si algo se salio de lo normal en un rango de dias. Necesito que me digas cuantos dias quieres revisar.
                - Revisar la evolucion de consumo para entender como fue cambiando con el tiempo. Necesito el codigo de la cuadrilla, el codigo del item y cuantos dias quieres revisar.
                - Hacer una proyeccion simple de consumo para estimar lo que podria venir despues. Necesito el codigo del item, los dias de historial y los dias hacia adelante que quieres proyectar.
                - Explicar que puede hacer el chatbot.
                - Ayudar a entender la pantalla actual. Necesito el contexto de la pantalla que estas viendo.
                - Mantener el hilo de la conversacion dentro de la sesion actual.

                No puedo:
                - Crear registros.
                - Modificar registros.
                - Aprobar procesos.
                - Eliminar informacion.
                - Ejecutar acciones operativas.
                - Responder libremente temas fuera del sistema, porque estoy limitado por el guard de dominio.
                """;
    }
}
