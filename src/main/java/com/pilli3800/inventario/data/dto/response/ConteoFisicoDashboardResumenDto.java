package com.pilli3800.inventario.data.dto.response;

public record ConteoFisicoDashboardResumenDto(
        Long totalConteos,
        Long totalItemsContados,
        Long itemsEvaluados,
        Long itemsConDiscrepancia,
        Long stockSistemaTotal,
        Long stockFisicoTotal,
        Long diferenciaAbsolutaTotal
) {
}
