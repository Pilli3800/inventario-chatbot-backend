package com.pilli3800.inventario.client;

import com.pilli3800.inventario.data.dto.response.ml.ConsumoAnomaliaResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoEvolucionResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoProyeccionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "consumo-ml-client",
        url = "${consumo.ml.url:http://127.0.0.1:8081}"
)
public interface ConsumoMlClient {

    @GetMapping("/ml/consumo/anomalias")
    ConsumoAnomaliaResponse obtenerAnomalias(
            @RequestParam Integer dias,
            @RequestParam Integer periodosHistorial,
            @RequestParam boolean guardarAlertas
    );

    @GetMapping("/ml/consumo/evolucion")
    ConsumoEvolucionResponse obtenerEvolucion(
            @RequestParam String cuadrillaCodigo,
            @RequestParam String itemCodigo,
            @RequestParam Integer dias,
            @RequestParam boolean guardarAlertas
    );

    @GetMapping("/ml/consumo/proyeccion")
    ConsumoProyeccionResponse obtenerProyeccion(
            @RequestParam String itemCodigo,
            @RequestParam Integer diasHist,
            @RequestParam Integer diasFuturo
    );
}
