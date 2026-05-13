package com.pilli3800.inventario.service;

import com.pilli3800.inventario.client.ConsumoMlClient;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoAnomaliaResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoEvolucionResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoProyeccionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumoMlService {

    private final ConsumoMlClient consumoMlClient;

    public ConsumoAnomaliaResponse obtenerAnomalias(
            Integer dias,
            Integer periodosHistorial,
            boolean guardarAlertas
    ) {
        return consumoMlClient.obtenerAnomalias(dias, periodosHistorial, guardarAlertas);
    }

    public ConsumoEvolucionResponse obtenerEvolucion(
            String cuadrillaCodigo,
            String itemCodigo,
            Integer dias,
            boolean guardarAlertas
    ) {
        return consumoMlClient.obtenerEvolucion(cuadrillaCodigo, itemCodigo, dias, guardarAlertas);
    }

    public ConsumoProyeccionResponse obtenerProyeccion(
            String itemCodigo,
            Integer diasHist,
            Integer diasFuturo
    ) {
        return consumoMlClient.obtenerProyeccion(itemCodigo, diasHist, diasFuturo);
    }
}
