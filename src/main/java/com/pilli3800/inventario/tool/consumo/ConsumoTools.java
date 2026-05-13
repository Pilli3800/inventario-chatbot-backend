package com.pilli3800.inventario.tool.consumo;

import com.pilli3800.inventario.data.dto.response.ml.ConsumoAnomaliaDto;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoAnomaliaResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoEvolucionDto;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoEvolucionResponse;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoProyeccionDto;
import com.pilli3800.inventario.data.dto.response.ml.ConsumoProyeccionResponse;
import com.pilli3800.inventario.service.ConsumoMlService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsumoTools {

    private final ConsumoMlService consumoMlService;

    @Tool(description = """
            Consulta consumos anomalos por cuadrilla e item.
            Usala cuando el usuario pregunte por consumos raros, anomalos o casos fuera de lo normal.
            """)
    @PreAuthorize("isAuthenticated()")
    public String obtenerAnomaliasConsumoRecientes(Integer dias) {
        ConsumoAnomaliaResponse response = consumoMlService.obtenerAnomalias(dias, 4, false);

        if (response == null || response.resultados() == null || response.resultados().isEmpty()) {
            return "No se encontraron resultados de consumo para el periodo consultado.";
        }

        List<ConsumoAnomaliaDto> anomalias = response.resultados().stream()
                .filter(dto -> Boolean.TRUE.equals(dto.isAnomaly()))
                .limit(5)
                .toList();

        if (anomalias.isEmpty()) {
            return "No se detectaron consumos anomalos en " + dias + " dia(s).";
        }

        StringBuilder respuesta = new StringBuilder();
        respuesta.append("Se detectaron ")
                .append(valor(response.totalAnomalias()))
                .append(" anomalia(s) de consumo en ")
                .append(dias)
                .append(" dia(s).")
                .append(System.lineSeparator())
                .append("Criterio: ")
                .append(valor(response.criterio() != null ? response.criterio().metodo() : null))
                .append(".")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        for (int i = 0; i < anomalias.size(); i++) {
            ConsumoAnomaliaDto anomalia = anomalias.get(i);
            respuesta.append(i + 1)
                    .append(". Cuadrilla ")
                    .append(valor(anomalia.cuadrillaCodigo()))
                    .append(", item ")
                    .append(valor(anomalia.itemCodigo()))
                    .append(" (")
                    .append(valor(anomalia.itemNombre()))
                    .append(").")
                    .append(System.lineSeparator())
                    .append("   Consumo actual: ")
                    .append(valor(anomalia.consumoActual()))
                    .append(", promedio: ")
                    .append(valor(anomalia.consumoPromedio()))
                    .append(", z-score: ")
                    .append(valor(anomalia.zScore()))
                    .append(".")
                    .append(System.lineSeparator())
                    .append("   Explicacion: ")
                    .append(valor(anomalia.explicacion()))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        ConsumoAnomaliaDto primeraAnomalia = anomalias.getFirst();
        respuesta.append("Si quieres continuar, puedo ayudarte con alguno de estos siguientes pasos:")
                .append(System.lineSeparator())
                .append("- Ver la evolucion del consumo de la cuadrilla ")
                .append(valor(primeraAnomalia.cuadrillaCodigo()))
                .append(" para el item ")
                .append(valor(primeraAnomalia.itemCodigo()))
                .append(".")
                .append(System.lineSeparator())
                .append("- Ver el ultimo movimiento del item ")
                .append(valor(primeraAnomalia.itemCodigo()))
                .append(".")
                .append(System.lineSeparator())
                .append("- Ver el historial del item ")
                .append(valor(primeraAnomalia.itemCodigo()))
                .append(".");

        return respuesta.toString();
    }

    @Tool(description = """
            Consulta la evolucion diaria del consumo de un item en una cuadrilla.
            Usala cuando el usuario quiera ver como se comporto el consumo en el tiempo.
            """)
    @PreAuthorize("isAuthenticated()")
    public String obtenerEvolucionConsumoPorCuadrillaEItem(
            String codigoCuadrilla,
            String codigoItem,
            Integer dias
    ) {
        ConsumoEvolucionResponse response = consumoMlService.obtenerEvolucion(
                codigoCuadrilla,
                codigoItem,
                dias,
                false
        );

        if (response == null || response.resultados() == null || response.resultados().isEmpty()) {
            return "No se encontro evolucion de consumo para la cuadrilla " + codigoCuadrilla
                    + " y el item " + codigoItem + ".";
        }

        List<ConsumoEvolucionDto> eventos = response.resultados().stream()
                .filter(dto -> Boolean.TRUE.equals(dto.eventoDestacado()))
                .limit(5)
                .toList();

        StringBuilder respuesta = new StringBuilder();
        respuesta.append("Evolucion del consumo para la cuadrilla ")
                .append(codigoCuadrilla)
                .append(" y el item ")
                .append(codigoItem)
                .append(" en ")
                .append(dias)
                .append(" dia(s).")
                .append(System.lineSeparator())
                .append("Consumo total: ")
                .append(valor(response.resumen() != null ? response.resumen().consumoTotal() : null))
                .append(", promedio diario: ")
                .append(valor(response.resumen() != null ? response.resumen().consumoPromedioDiario() : null))
                .append(".")
                .append(System.lineSeparator());

        if (eventos.isEmpty()) {
            respuesta.append("No se detectaron eventos destacados en ese periodo.")
                    .append(System.lineSeparator())
                    .append("Si quieres, tambien puedo revisar el ultimo movimiento del item ")
                    .append(codigoItem)
                    .append(" o mostrarte su historial.");
            return respuesta.toString();
        }

        respuesta.append("Se detectaron ")
                .append(eventos.size())
                .append(" evento(s) destacado(s):")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        for (int i = 0; i < eventos.size(); i++) {
            ConsumoEvolucionDto evento = eventos.get(i);
            respuesta.append(i + 1)
                    .append(". Fecha: ")
                    .append(valor(evento.fecha()))
                    .append(", consumo diario: ")
                    .append(valor(evento.consumoDiario()))
                    .append(", tendencia: ")
                    .append(valor(evento.tendencia()))
                    .append(".")
                    .append(System.lineSeparator())
                    .append("   Explicacion: ")
                    .append(valor(evento.explicacion()))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        respuesta.append("Si quieres continuar, puedo revisar el ultimo movimiento del item ")
                .append(codigoItem)
                .append(" o mostrarte su historial.");

        return respuesta.toString().trim();
    }

    @Tool(description = """
            Consulta una proyeccion simple de consumo futuro de un item.
            Usala cuando el usuario pregunte cuanto podria consumirse si el patron actual se mantiene.
            """)
    @PreAuthorize("isAuthenticated()")
    public String obtenerProyeccionConsumo(
            String codigoItem,
            Integer diasHist,
            Integer diasFuturo
    ) {
        ConsumoProyeccionResponse response = consumoMlService.obtenerProyeccion(
                codigoItem,
                diasHist,
                diasFuturo
        );

        if (response == null || response.resultados() == null || response.resultados().isEmpty()) {
            return "No se encontro proyeccion de consumo para el item " + codigoItem + ".";
        }

        List<ConsumoProyeccionDto> proyecciones = response.resultados().stream()
                .limit(5)
                .toList();

        Double consumoPromedioDiario = response.resumen() != null
                ? response.resumen().consumoPromedioDiario()
                : null;
        Double consumoEstimadoPeriodo = consumoPromedioDiario != null
                ? consumoPromedioDiario * diasFuturo
                : null;
        ConsumoProyeccionDto primeraProyeccion = response.resultados().getFirst();
        ConsumoProyeccionDto ultimaProyeccion = response.resultados().getLast();

        StringBuilder respuesta = new StringBuilder();
        respuesta.append("Proyeccion simple de consumo para el item ")
                .append(codigoItem)
                .append(".")
                .append(System.lineSeparator())
                .append("Tomando como referencia los ultimos ")
                .append(diasHist)
                .append(" dia(s), se estima un consumo promedio de ")
                .append(valor(consumoPromedioDiario))
                .append(" unidad(es) por dia.")
                .append(".")
                .append(System.lineSeparator())
                .append("Si el patron se mantiene, en los proximos ")
                .append(diasFuturo)
                .append(" dia(s) podrian consumirse aproximadamente ")
                .append(valor(consumoEstimadoPeriodo))
                .append(" unidad(es).")
                .append(System.lineSeparator())
                .append("La proyeccion cubre desde ")
                .append(valor(primeraProyeccion.fecha()))
                .append(" hasta ")
                .append(valor(ultimaProyeccion.fecha()))
                .append(".")
                .append(System.lineSeparator())
                .append("Metodo usado: ")
                .append(valor(response.criterio() != null ? response.criterio().metodo() : null))
                .append(".")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        for (int i = 0; i < proyecciones.size(); i++) {
            ConsumoProyeccionDto proyeccion = proyecciones.get(i);
            respuesta.append(i + 1)
                    .append(". Fecha: ")
                    .append(valor(proyeccion.fecha()))
                    .append(", consumo estimado del dia: ")
                    .append(valor(proyeccion.consumoEstimado()))
                    .append(".")
                    .append(System.lineSeparator())
                    .append("   Interpretacion: ")
                    .append(valor(proyeccion.explicacion()))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        respuesta.append("Si quieres, tambien puedo revisar el ultimo movimiento del item ")
                .append(codigoItem)
                .append(" o mostrarte su historial.");

        if (response.explicacionGeneral() != null && !response.explicacionGeneral().isBlank()) {
            respuesta.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("Nota: ")
                    .append(response.explicacionGeneral());
        }

        return respuesta.toString().trim();
    }

    private String valor(Object valor) {
        return valor == null ? "sin dato" : valor.toString();
    }
}
