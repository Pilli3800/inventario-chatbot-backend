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
            No ejecuta acciones ni registra informacion; solo explica y guia con lenguaje natural.
            Debe ayudar a entender de forma simple que pantalla esta viendo y que puede hacer ahi.
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
                .append("Responde en lenguaje natural, claro y cercano, para que el usuario entienda rapido que esta viendo y que puede hacer ahi. ")
                .append("Usa un maximo de dos parrafos cortos. ")
                .append("No uses tablas, listas largas, titulos decorativos ni emojis salvo que el usuario los pida. ")
                .append("No repitas todo lo visible; resume solo lo importante para entender la pantalla. ")
                .append("No inventes estados, filtros, botones, metricas, pestañas ni acciones que no aparezcan en el contexto. ")
                .append("Si el contexto no alcanza para explicar algo, dilo con naturalidad y responde solo con lo que si se ve. ")
                .append("No digas que puedes crear, aprobar, cerrar, eliminar o ejecutar acciones por el usuario. ")
                .append("Explica primero para que sirve la pantalla y luego responde la duda concreta. ")
                .append("Termina preguntando que parte especifica necesita entender. ")
                .append("Si mencionas acciones, usa solo acciones presentes en el contexto.");

        return ayuda.toString();
    }

    private String describirPantalla(String ruta, String titulo, String modulo) {
        String texto = (valor(ruta, "") + " " + valor(titulo, "") + " " + valor(modulo, "")).toLowerCase();

        if (texto.contains("solicitud")) {
            return "En esta pantalla el usuario revisa solicitudes del sistema. "
                    + "Debes ayudarle a entender de forma simple para que sirve, que informacion ve y que opciones tiene disponibles segun el contexto.";
        }

        if (texto.contains("movimiento")) {
            return "En esta pantalla el usuario revisa movimientos de inventario. "
                    + "Debes ayudarle a interpretar lo que ve y a entender filtros, origen, destino, item, cantidad y fechas solo si aparecen en el contexto.";
        }

        if (texto.contains("inventario") || texto.contains("stock")) {
            return "En esta pantalla el usuario revisa stock o disponibilidad. "
                    + "Debes ayudarle a entender de manera sencilla que datos puede consultar y como leer los resultados visibles.";
        }

        if (texto.contains("item")) {
            return "En esta pantalla el usuario revisa informacion de items. "
                    + "Debes ayudarle a explicar que ve en la pantalla y que puede consultar sobre esos items sin inventar datos que no esten visibles.";
        }

        if (texto.contains("usuario")) {
            return "En esta pantalla el usuario revisa informacion de usuarios. "
                    + "Debes ayudarle a entender los campos visibles, los filtros y los estados, sin sugerir cambios ni acciones que no esten disponibles.";
        }

        return "Explica de forma general para que sirve la pantalla actual usando solo la ruta, titulo, modulo, "
                + "elementos visibles y acciones disponibles enviados por el frontend. "
                + "Si falta informacion, dilo con claridad y no completes con suposiciones.";
    }

    private boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String valor(String valor, String valorPorDefecto) {
        return tieneValor(valor) ? valor.trim() : valorPorDefecto;
    }
}
