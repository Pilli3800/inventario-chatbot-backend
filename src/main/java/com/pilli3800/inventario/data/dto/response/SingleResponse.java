package com.pilli3800.inventario.data.dto.response;

import java.time.Instant;

public record SingleResponse<T>(
        int status,
        String path,
        T data,
        Instant generatedAt
    ) {

    public SingleResponse(int status, String path, T data) {
        this(status, path, data, Instant.now());
    }
}
