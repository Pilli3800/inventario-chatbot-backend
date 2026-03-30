package com.pilli3800.inventario.util;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public final class DateFormat {

    private static final DateTimeFormatter FORMATO_FECHA_HORA_PDF =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(FORMATO_FECHA_HORA_PDF) : "";
    }
}
