package com.pilli3800.inventario.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TextFormat {

    public static String reemplazarNuloPorVacio(String valor) {
        return valor != null ? valor : "";
    }
}
