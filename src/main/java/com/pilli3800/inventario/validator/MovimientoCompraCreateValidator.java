package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.movimientos.MovimientoCompraCreateRequest;
import com.pilli3800.inventario.data.models.Proveedor;
import com.pilli3800.inventario.data.models.item.Item;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.ItemRepository;
import com.pilli3800.inventario.repository.ProveedorRepository;
import com.pilli3800.inventario.util.RucValidator;
import com.pilli3800.inventario.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovimientoCompraCreateValidator {

    private static final String NUMERO_FACTURA_COMPLETO_REGEX = "^\\d{11}-[A-Z0-9]{1,4}-\\d{1,8}$";
    private static final String SERIE_REGEX = "^[A-Z0-9]{1,4}$";
    private static final String CORRELATIVO_REGEX = "^\\d{1,8}$";

    private final ItemRepository itemRepository;
    private final ProveedorRepository proveedorRepository;

    public void validate(MovimientoCompraCreateRequest request) {

        List<String> errors = new ArrayList<>();

        if (request.cantidad() == null || request.cantidad() <= 0) {
            errors.add("La cantidad debe ser mayor a cero");
        }

        if (request.sedeDestinoCodigo() == null || request.sedeDestinoCodigo().isBlank()) {
            errors.add("La compra debe indicar sede destino");
        }

        String codigoItem = TextNormalizer.normalizeCode(request.codigoItem());
        Item item = itemRepository
                .findByCodigoItem(codigoItem)
                .orElse(null);

        if (item == null) {
            errors.add("El item no existe");
        } else if (!item.isEnabled()) {
            errors.add("El item esta desactivado");
        }

        String codigoProveedor = TextNormalizer.normalizeCode(request.codigoProveedor());
        Proveedor proveedor = proveedorRepository
                .findByCodigo(codigoProveedor)
                .orElse(null);

        if (proveedor == null) {
            errors.add("El proveedor no existe");
        } else if (!proveedor.isEnabled()) {
            errors.add("El proveedor esta desactivado");
        } else if (!RucValidator.isValid(proveedor.getRuc())) {
            errors.add("El proveedor tiene RUC invalido");
        }

        String numeroFactura = request.numeroFactura() == null
                ? null
                : request.numeroFactura().trim().toUpperCase(java.util.Locale.ROOT);
        String serieFactura = request.serieFactura() == null
                ? null
                : request.serieFactura().trim().toUpperCase(java.util.Locale.ROOT);
        String correlativoFactura = request.correlativoFactura() == null
                ? null
                : request.correlativoFactura().trim();

        boolean tieneNumeroCompleto = numeroFactura != null && !numeroFactura.isBlank();
        boolean tieneSerieCorrelativo = serieFactura != null && !serieFactura.isBlank()
                && correlativoFactura != null && !correlativoFactura.isBlank();

        if (tieneNumeroCompleto == tieneSerieCorrelativo) {
            errors.add("Debe enviar numeroFactura o serieFactura+correlativoFactura");
        }

        if (tieneNumeroCompleto && !numeroFactura.matches(NUMERO_FACTURA_COMPLETO_REGEX)) {
            errors.add("El numero de factura tiene formato invalido");
        }

        if (tieneSerieCorrelativo) {
            if (!serieFactura.matches(SERIE_REGEX)) {
                errors.add("La serie de factura tiene formato invalido");
            }
            if (!correlativoFactura.matches(CORRELATIVO_REGEX)) {
                errors.add("El correlativo de factura tiene formato invalido");
            }
        }

        if (tieneNumeroCompleto && proveedor != null) {
            String prefijoEsperado = proveedor.getRuc() + "-";
            if (!numeroFactura.startsWith(prefijoEsperado)) {
                errors.add("El numero de factura no corresponde al proveedor");
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            errors.add("Usuario no autenticado");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
