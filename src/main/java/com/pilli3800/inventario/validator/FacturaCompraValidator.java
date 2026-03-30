package com.pilli3800.inventario.validator;

import com.pilli3800.inventario.data.dto.request.FacturaCompraCreateRequest;
import com.pilli3800.inventario.data.models.Proveedor;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.FacturaCompraRepository;
import com.pilli3800.inventario.repository.ProveedorRepository;
import com.pilli3800.inventario.util.RucValidator;
import com.pilli3800.inventario.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FacturaCompraValidator {

    private static final String NUMERO_FACTURA_COMPLETO_REGEX = "^\\d{11}-[A-Z0-9]{1,4}-\\d{1,8}$";
    private static final String SERIE_REGEX = "^[A-Z0-9]{1,4}$";
    private static final String CORRELATIVO_REGEX = "^\\d{1,8}$";

    private final ProveedorRepository proveedorRepository;
    private final FacturaCompraRepository facturaCompraRepository;

    public void validateCreate(FacturaCompraCreateRequest request) {

        List<String> errors = new ArrayList<>();

        String codigoProveedor = TextNormalizer.normalizeCode(request.codigoProveedor());
        Proveedor proveedor = proveedorRepository
                .findByCodigo(codigoProveedor)
                .orElse(null);

        if (proveedor == null) {
            errors.add("El proveedor no existe");
        } else {
            if (!proveedor.isEnabled()) {
                errors.add("El proveedor esta desactivado");
            }

            if (!RucValidator.isValid(proveedor.getRuc())) {
                errors.add("El proveedor tiene RUC invalido");
            }

            String numeroFactura = request.numeroFactura() == null
                    ? null
                    : request.numeroFactura().trim().toUpperCase(java.util.Locale.ROOT);
            String serie = request.serie() == null
                    ? null
                    : request.serie().trim().toUpperCase(java.util.Locale.ROOT);
            String correlativo = request.correlativo() == null
                    ? null
                    : request.correlativo().trim();

            boolean tieneNumeroCompleto = numeroFactura != null && !numeroFactura.isBlank();
            boolean tieneSerieCorrelativo = serie != null && !serie.isBlank()
                    && correlativo != null && !correlativo.isBlank();

            if (tieneNumeroCompleto == tieneSerieCorrelativo) {
                errors.add("Debe enviar numeroFactura o serie+correlativo");
            }

            if (tieneNumeroCompleto) {
                if (!numeroFactura.matches(NUMERO_FACTURA_COMPLETO_REGEX)) {
                    errors.add("El numero de factura tiene formato invalido");
                } else {
                    String prefijoEsperado = proveedor.getRuc() + "-";
                    if (!numeroFactura.startsWith(prefijoEsperado)) {
                        errors.add("El numero de factura no corresponde al proveedor");
                    }
                }

                if (facturaCompraRepository.existsByProveedorAndNumeroFactura(proveedor, numeroFactura)) {
                    errors.add("La factura ya existe para este proveedor");
                }
            }

            if (tieneSerieCorrelativo) {
                if (!serie.matches(SERIE_REGEX)) {
                    errors.add("La serie tiene formato invalido");
                }
                if (!correlativo.matches(CORRELATIVO_REGEX)) {
                    errors.add("El correlativo tiene formato invalido");
                }
                if (facturaCompraRepository.existsByProveedorAndSerieAndCorrelativo(proveedor, serie, correlativo)) {
                    errors.add("La factura ya existe para este proveedor");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
