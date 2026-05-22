package com.pilli3800.inventario.tool.chat;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class SoporteUsuarioTools {

    @Tool(description = """
            Orienta al usuario sobre la pantalla actual del sistema.
            Usala cuando el usuario pida ayuda, diga que no entiende una pantalla,
            pregunte que puede hacer aqui o necesite una guia de uso.
            No ejecuta acciones ni registra informacion; solo explica y guia.
            Debe responder de forma breve y natural, no como manual largo.
            """)
    @PreAuthorize("isAuthenticated()")
    public String obtenerAyudaPantallaActual(
            String ruta,
            String titulo,
            String modulo,
            String elementosVisibles,
            String accionesDisponibles
    ) {
        StringBuilder ayuda = new StringBuilder();
        ayuda.append("Estas orientando al usuario en la pantalla actual del sistema.")
                .append(System.lineSeparator())
                .append("Pantalla: ").append(valor(titulo, "sin titulo")).append(System.lineSeparator())
                .append("Modulo: ").append(valor(modulo, "sin modulo")).append(System.lineSeparator())
                .append("Ruta: ").append(valor(ruta, "sin ruta")).append(System.lineSeparator());

        if (tieneValor(elementosVisibles)) {
            ayuda.append("Elementos visibles: ").append(elementosVisibles).append(System.lineSeparator());
        }

        if (tieneValor(accionesDisponibles)) {
            ayuda.append("Acciones disponibles: ").append(accionesDisponibles).append(System.lineSeparator());
        }

        ayuda.append(System.lineSeparator())
                .append(describirPantalla(ruta, titulo, modulo))
                .append(System.lineSeparator())
                .append("Responde con maximo dos parrafos cortos. ")
                .append("No uses tablas, listas largas, titulos decorativos ni emojis salvo que el usuario los pida. ")
                .append("No repitas todos los elementos visibles ni todas las acciones disponibles. ")
                .append("Explica el objetivo de la pantalla y el flujo principal. ")
                .append("Termina preguntando que parte especifica necesita entender. ")
                .append("Si mencionas acciones, usa solo acciones presentes en el contexto.");

        return ayuda.toString();
    }

    private String describirPantalla(String ruta, String titulo, String modulo) {
        String texto = (valor(ruta, "") + " " + valor(titulo, "") + " " + valor(modulo, "")).toLowerCase();

        if (texto.contains("solicitud")) {
            return "En esta pantalla el usuario puede revisar o preparar solicitudes de items. "
                    + "Debes ayudarle a entender campos como servicio, cuadrilla, items, cantidades, estado y observaciones.";
        }

        if (texto.contains("movimiento")) {
            return "En esta pantalla el usuario puede revisar movimientos de inventario. "
                    + "Debes ayudarle a interpretar tipos de movimiento, filtros, origen, destino, item, cantidad y fechas.";
        }

        if (texto.contains("inventario") || texto.contains("stock")) {
            return "En esta pantalla el usuario puede revisar stock o disponibilidad. "
                    + "Debes ayudarle a entender busquedas por item, sede, servicio, cantidades disponibles y resultados.";
        }

        if (texto.contains("item")) {
            return "En esta pantalla el usuario puede revisar informacion de items. "
                    + "Debes ayudarle a entender codigo, nombre, tipo, descripcion, estado y filtros de busqueda.";
        }

        if (texto.contains("usuario")) {
            return "En esta pantalla el usuario puede revisar informacion de usuarios. "
                    + "Debes ayudarle a entender campos visibles, filtros, estados y roles sin modificar datos.";
        }

        return "Explica de forma general para que sirve la pantalla actual segun la ruta, titulo, modulo, "
                + "elementos visibles y acciones disponibles enviados por el frontend.";
    }

    private boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String valor(String valor, String valorPorDefecto) {
        return tieneValor(valor) ? valor.trim() : valorPorDefecto;
    }
}
