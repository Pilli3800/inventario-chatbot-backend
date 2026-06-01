package com.pilli3800.inventario.data.dto.response.ml;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDateTime;

public record AlertaMlDto(
        Long id,

        String tipo,

        String descripcion,

        @JsonAlias("referencia_tipo")
        String referenciaTipo,

        @JsonAlias("referencia_codigo")
        String referenciaCodigo,

        @JsonAlias("fecha_alerta")
        LocalDateTime fechaAlerta
) {
}
