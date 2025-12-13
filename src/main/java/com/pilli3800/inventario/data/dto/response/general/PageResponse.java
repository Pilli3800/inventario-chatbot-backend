package com.pilli3800.inventario.data.dto.response.general;

import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public record PageResponse<T>(
        Instant timestamp,
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                Instant.now(),
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
