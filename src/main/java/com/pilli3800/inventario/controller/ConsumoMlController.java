package com.pilli3800.inventario.controller;

import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import com.pilli3800.inventario.data.dto.response.ml.AlertaMlDto;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoAnomaliaResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoEvolucionResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoProyeccionResponse;
import com.pilli3800.inventario.service.ConsumoMlService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/consumo/ml")
@RequiredArgsConstructor
@Validated
public class ConsumoMlController {

    private final ConsumoMlService consumoMlService;

    @GetMapping("/anomalias")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ConsumoAnomaliaResponse> getAnomalias(
            @RequestParam(defaultValue = "7") @Min(1) @Max(30) Integer dias,
            @RequestParam(defaultValue = "4") @Min(2) @Max(12) Integer periodosHistorial,
            @RequestParam(defaultValue = "false") boolean guardarAlertas
    ) {
        return new SingleResponse<>(
                200,
                "/api/consumo/ml/anomalias",
                consumoMlService.obtenerAnomalias(dias, periodosHistorial, guardarAlertas)
        );
    }

    @GetMapping("/evolucion")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ConsumoEvolucionResponse> getEvolucion(
            @RequestParam @NotBlank String cuadrillaCodigo,
            @RequestParam @NotBlank String itemCodigo,
            @RequestParam(defaultValue = "30") @Min(7) @Max(120) Integer dias,
            @RequestParam(defaultValue = "false") boolean guardarAlertas
    ) {
        return new SingleResponse<>(
                200,
                "/api/consumo/ml/evolucion",
                consumoMlService.obtenerEvolucion(cuadrillaCodigo, itemCodigo, dias, guardarAlertas)
        );
    }

    @GetMapping("/proyeccion")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<ConsumoProyeccionResponse> getProyeccion(
            @RequestParam @NotBlank String itemCodigo,
            @RequestParam(defaultValue = "60") @Min(15) @Max(180) Integer diasHist,
            @RequestParam(defaultValue = "15") @Min(1) @Max(60) Integer diasFuturo
    ) {
        return new SingleResponse<>(
                200,
                "/api/consumo/ml/proyeccion",
                consumoMlService.obtenerProyeccion(itemCodigo, diasHist, diasFuturo)
        );
    }

    @GetMapping("/alertas")
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<List<AlertaMlDto>> getAlertas(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String referenciaTipo,
            @RequestParam(required = false) String referenciaCodigo,
            @RequestParam(name = "referencia_tipo", required = false) String referenciaTipoSnake,
            @RequestParam(name = "referencia_codigo", required = false) String referenciaCodigoSnake,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer limit
    ) {
        return new SingleResponse<>(
                200,
                "/api/consumo/ml/alertas",
                consumoMlService.obtenerAlertas(
                        tipo,
                        valorPreferido(referenciaTipo, referenciaTipoSnake),
                        valorPreferido(referenciaCodigo, referenciaCodigoSnake),
                        fechaInicio,
                        fechaFin,
                        limit
                )
        );
    }

    private String valorPreferido(String principal, String alternativo) {
        return principal != null && !principal.isBlank()
                ? principal
                : alternativo;
    }
}
