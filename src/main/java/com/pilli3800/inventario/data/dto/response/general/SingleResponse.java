package com.pilli3800.inventario.data.dto.response.general;

import java.time.Instant;

public record SingleResponse<T>(
        Instant timestamp,
        int status,
        String path,
        T content
    ) {

    public SingleResponse(int status, String path, T content) {
        this(Instant.now(), status, path, content);
    }
}
