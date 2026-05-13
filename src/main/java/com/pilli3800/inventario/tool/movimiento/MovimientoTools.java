package com.pilli3800.inventario.tool.movimiento;

import com.pilli3800.inventario.data.dto.response.ItemHistorialMovimientoDto;
import com.pilli3800.inventario.data.dto.response.MovimientoInventarioDto;
import com.pilli3800.inventario.data.dto.response.general.CodigoNombreDto;
import com.pilli3800.inventario.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovimientoTools {

    private final ItemService itemService;

    @Tool(description = """
            Obtiene solo el ultimo movimiento de un item por su codigo exacto.
            Usala cuando el usuario pregunte por el ultimo movimiento de un codigoItem.
            """)
    @PreAuthorize("isAuthenticated()")
    public String obtenerUltimoMovimientoPorCodigoItem(String codigoItem) {
        MovimientoInventarioDto ultimoMovimiento = itemService.getUltimoMovimientoItem(codigoItem);
        String enlaceDetalle = "http://localhost:5173/logistica/movimientos/ver/" + ultimoMovimiento.id();

        return """
                El ultimo movimiento del item %s (%s) fue una %s.
                Se movieron %d unidad(es).
                %s
                Lo registro el usuario %s el %s.
                Puedes ver el detalle aqui: %s
                """.formatted(
                ultimoMovimiento.nombreItem(),
                ultimoMovimiento.codigoItem(),
                ultimoMovimiento.tipoMovimiento().toLowerCase(),
                ultimoMovimiento.cantidad(),
                describirUbicacionMovimiento(ultimoMovimiento),
                ultimoMovimiento.usuario(),
                ultimoMovimiento.fechaMovimiento(),
                enlaceDetalle
        );
    }

    @Tool(description = """
            Obtiene el historial reciente de movimientos de un item por su codigo exacto.
            Usala cuando el usuario quiera ver el historial de un codigoItem.
            """)
    @PreAuthorize("isAuthenticated()")
    public String obtenerHistorialMovimientoPorCodigoItem(String codigoItem) {
        List<ItemHistorialMovimientoDto> historial = itemService.getHistorialItem(codigoItem);

        if (historial.isEmpty()) {
            return "No se encontraron movimientos para el item " + codigoItem + ".";
        }

        List<ItemHistorialMovimientoDto> ultimosMovimientos = historial.stream()
                .limit(5)
                .toList();

        ItemHistorialMovimientoDto primerMovimiento = ultimosMovimientos.getFirst();

        List<String> movimientosFormateados = new ArrayList<>();
        for (int i = 0; i < ultimosMovimientos.size(); i++) {
            movimientosFormateados.add(
                    formatearMovimientoHistorial(ultimosMovimientos.get(i), i + 1)
            );
        }

        return "**Historial del item " + primerMovimiento.codigoItem() + "**"
                + System.lineSeparator()
                + System.lineSeparator()
                + String.join(System.lineSeparator() + System.lineSeparator(), movimientosFormateados)
                + System.lineSeparator()
                + System.lineSeparator()
                + "Estos son los 5 movimientos mas recientes; el historial completo contiene "
                + historial.size() + " registro(s).";
    }

    private String describirUbicacionMovimiento(MovimientoInventarioDto movimiento) {
        return switch (movimiento.tipoMovimiento()) {
            case "ENTRADA", "COMPRA" ->
                    "El item ingreso a la sede " + valorOmitible(movimiento.sedeDestino()) + ".";
            case "SALIDA" ->
                    "El item salio de la sede " + valorOmitible(movimiento.sedeOrigen())
                            + " para la cuadrilla " + valorOmitible(movimiento.codigoCuadrilla()) + ".";
            case "TRANSFERENCIA" ->
                    "El item fue transferido desde la sede " + valorOmitible(movimiento.sedeOrigen())
                            + " hacia la sede " + valorOmitible(movimiento.sedeDestino()) + ".";
            case "TRANSFERENCIA_SERVICIO" ->
                    "El item fue transferido desde la sede " + valorOmitible(movimiento.sedeOrigen())
                            + " hacia el servicio " + valorServicio(movimiento.servicio()) + ".";
            case "SALIDA_CUADRILLA" ->
                    "El item salio del servicio " + valorServicio(movimiento.servicio())
                            + " para la cuadrilla " + valorOmitible(movimiento.codigoCuadrilla()) + ".";
            case "RETORNO_A_SEDE" ->
                    "El item retorno desde el servicio " + valorServicio(movimiento.servicio())
                            + " hacia la sede " + valorOmitible(movimiento.sedeDestino()) + ".";
            case "DEVOLUCION" ->
                    "El item fue devuelto por la cuadrilla " + valorOmitible(movimiento.codigoCuadrilla())
                            + " al servicio " + valorServicio(movimiento.servicio()) + ".";
            default ->
                    "El movimiento fue registrado sin una descripcion de ubicacion disponible.";
        };
    }

    private String describirUbicacionMovimiento(ItemHistorialMovimientoDto movimiento) {
        return switch (movimiento.tipoMovimiento()) {
            case "ENTRADA", "COMPRA" ->
                    "El item ingreso a la sede " + valorOmitible(movimiento.sedeDestino()) + ".";
            case "SALIDA" ->
                    "El item salio de la sede " + valorOmitible(movimiento.sedeOrigen())
                            + " para la cuadrilla " + valorOmitible(movimiento.codigoCuadrilla()) + ".";
            case "TRANSFERENCIA" ->
                    "El item fue transferido desde la sede " + valorOmitible(movimiento.sedeOrigen())
                            + " hacia la sede " + valorOmitible(movimiento.sedeDestino()) + ".";
            case "TRANSFERENCIA_SERVICIO" ->
                    "El item fue transferido desde la sede " + valorOmitible(movimiento.sedeOrigen())
                            + " hacia el servicio " + valorServicio(movimiento.servicio()) + ".";
            case "SALIDA_CUADRILLA" ->
                    "El item salio del servicio " + valorServicio(movimiento.servicio())
                            + " para la cuadrilla " + valorOmitible(movimiento.codigoCuadrilla()) + ".";
            case "RETORNO_A_SEDE" ->
                    "El item retorno desde el servicio " + valorServicio(movimiento.servicio())
                            + " hacia la sede " + valorOmitible(movimiento.sedeDestino()) + ".";
            case "DEVOLUCION" ->
                    "El item fue devuelto por la cuadrilla " + valorOmitible(movimiento.codigoCuadrilla())
                            + " al servicio " + valorServicio(movimiento.servicio()) + ".";
            default ->
                    "El movimiento fue registrado sin una descripcion de ubicacion disponible.";
        };
    }

    private String valorServicio(CodigoNombreDto servicio) {
        return servicio != null
                ? servicio.codigo() + " (" + servicio.nombre() + ")"
                : "sin servicio asociado";
    }

    private String valorOmitible(String valor) {
        return valor == null || valor.isBlank()
                ? "no especificada"
                : valor;
    }

    private String formatearMovimientoHistorial(ItemHistorialMovimientoDto movimiento, int indice) {
        String enlaceDetalle = "http://localhost:5173/logistica/movimientos/ver/" + movimiento.id();

        return """
                %d. **%s** - %d unid
                   Fecha: %s
                   Usuario: %s
                   Detalle: %s
                   [Ver movimiento %d](%s)
                """.formatted(
                indice,
                movimiento.tipoMovimiento(),
                movimiento.cantidad(),
                movimiento.fechaMovimiento(),
                movimiento.usuario(),
                describirUbicacionMovimiento(movimiento),
                movimiento.id(),
                enlaceDetalle
        ).trim();
    }
}
