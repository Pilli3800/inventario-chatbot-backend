package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.response.general.HealthResponse;
import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public SingleResponse<HealthResponse> health() {
        return new SingleResponse<>(
                200,
                "/api/health",
                new HealthResponse(
                        "ok",
                        OffsetDateTime.now().toString()
                )
        );
    }
}
