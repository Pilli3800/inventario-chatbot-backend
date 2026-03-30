package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.FacturaCompraCreateRequest;
import com.pilli3800.inventario.data.dto.request.FacturaCompraSearchRequest;
import com.pilli3800.inventario.data.dto.response.FacturaCompraDto;
import com.pilli3800.inventario.data.models.FacturaCompra;
import com.pilli3800.inventario.data.models.Proveedor;
import com.pilli3800.inventario.exception.ValidationException;
import com.pilli3800.inventario.repository.FacturaCompraRepository;
import com.pilli3800.inventario.repository.ProveedorRepository;
import com.pilli3800.inventario.specifications.FacturaCompraSpecifications;
import com.pilli3800.inventario.util.RucValidator;
import com.pilli3800.inventario.util.TextNormalizer;
import com.pilli3800.inventario.validator.FacturaCompraValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaCompraService {

    private final FacturaCompraRepository facturaCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final FacturaCompraValidator facturaCompraValidator;

    public FacturaCompraDto createFactura(FacturaCompraCreateRequest request) {
        facturaCompraValidator.validateCreate(request);

        String codigoProveedor = TextNormalizer.normalizeCode(request.codigoProveedor());
        Proveedor proveedor = proveedorRepository.findByCodigo(codigoProveedor)
                .orElseThrow(() -> new ValidationException(List.of("Proveedor no encontrado")));
        DatosFactura datosFactura = resolverDatosFactura(
                proveedor,
                request.numeroFactura(),
                request.serie(),
                request.correlativo()
        );

        FacturaCompra factura = new FacturaCompra();
        factura.setProveedor(proveedor);
        factura.setNumeroFactura(datosFactura.numeroFactura());
        factura.setSerie(datosFactura.serie());
        factura.setCorrelativo(datosFactura.correlativo());
        factura.setFechaEmision(request.fechaEmision());
        factura.setObservaciones(request.observaciones());

        return FacturaCompraDto.from(facturaCompraRepository.save(factura));
    }

    public Page<FacturaCompraDto> getFacturas(FacturaCompraSearchRequest request, Pageable pageable) {
        Specification<FacturaCompra> spec = FacturaCompraSpecifications.search(request);
        return facturaCompraRepository.findAll(spec, pageable).map(FacturaCompraDto::from);
    }

    public FacturaCompraDto getFactura(String codigoProveedor, String numeroFactura) {
        return FacturaCompraDto.from(
                obtenerExistentePorProveedorYNumero(codigoProveedor, numeroFactura)
        );
    }

    public FacturaCompra obtenerOCrearParaCompra(
            String codigoProveedor,
            String numeroFactura,
            String serieFactura,
            String correlativoFactura,
            java.time.LocalDate fechaEmision
    ) {
        String codigoProveedorNormalizado = TextNormalizer.normalizeCode(codigoProveedor);

        Proveedor proveedor = proveedorRepository.findByCodigo(codigoProveedorNormalizado)
                .orElseThrow(() -> new ValidationException(List.of("Proveedor no encontrado")));

        if (!RucValidator.isValid(proveedor.getRuc())) {
            throw new ValidationException(List.of("El proveedor tiene RUC invalido"));
        }

        DatosFactura datosFactura = resolverDatosFactura(
                proveedor,
                numeroFactura,
                serieFactura,
                correlativoFactura
        );

        return facturaCompraRepository
                .findByProveedorAndNumeroFactura(proveedor, datosFactura.numeroFactura())
                .map(factura -> {
                    // Si no tenia fecha y ahora llega, se completa la cabecera
                    if (factura.getFechaEmision() == null && fechaEmision != null) {
                        factura.setFechaEmision(fechaEmision);
                        return facturaCompraRepository.save(factura);
                    }
                    return factura;
                })
                .orElseGet(() -> {
                    FacturaCompra factura = new FacturaCompra();
                    factura.setProveedor(proveedor);
                    factura.setNumeroFactura(datosFactura.numeroFactura());
                    factura.setSerie(datosFactura.serie());
                    factura.setCorrelativo(datosFactura.correlativo());
                    factura.setFechaEmision(fechaEmision);
                    // Campo reservado para futura integracion de XML/PDF
                    factura.setDocumentoReferencia(null);
                    return facturaCompraRepository.save(factura);
                });
    }

    private FacturaCompra obtenerExistentePorProveedorYNumero(String codigoProveedor, String numeroFactura) {
        String codigoProveedorNormalizado = TextNormalizer.normalizeCode(codigoProveedor);
        String numeroFacturaNormalizado = normalizarNumeroFactura(numeroFactura);

        Proveedor proveedor = proveedorRepository.findByCodigo(codigoProveedorNormalizado)
                .orElseThrow(() -> new ValidationException(List.of("Proveedor no encontrado")));

        return facturaCompraRepository
                .findByProveedorAndNumeroFactura(proveedor, numeroFacturaNormalizado)
                .orElseThrow(() -> new ValidationException(List.of("Factura no encontrada")));
    }

    private String normalizarNumeroFactura(String numeroFactura) {
        if (numeroFactura == null) {
            return null;
        }
        return numeroFactura.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String normalizarSerie(String serie) {
        if (serie == null) {
            return null;
        }
        return serie.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String normalizarCorrelativo(String correlativo) {
        if (correlativo == null) {
            return null;
        }
        return correlativo.trim();
    }

    private DatosFactura resolverDatosFactura(
            Proveedor proveedor,
            String numeroFactura,
            String serie,
            String correlativo
    ) {
        String numeroFacturaNormalizado = normalizarNumeroFactura(numeroFactura);
        String serieNormalizada = normalizarSerie(serie);
        String correlativoNormalizado = normalizarCorrelativo(correlativo);

        boolean tieneNumero = numeroFacturaNormalizado != null && !numeroFacturaNormalizado.isBlank();
        boolean tieneSerieCorrelativo = serieNormalizada != null && !serieNormalizada.isBlank()
                && correlativoNormalizado != null && !correlativoNormalizado.isBlank();

        if (tieneNumero == tieneSerieCorrelativo) {
            throw new ValidationException(List.of("Debe enviar numeroFactura o serieFactura+correlativoFactura"));
        }

        if (tieneNumero) {
            String[] partes = numeroFacturaNormalizado.split("-");
            if (partes.length != 3) {
                throw new ValidationException(List.of("El numero de factura tiene formato invalido"));
            }
            if (!partes[0].equals(proveedor.getRuc())) {
                throw new ValidationException(List.of("El numero de factura no corresponde al proveedor"));
            }

            return new DatosFactura(numeroFacturaNormalizado, partes[1], partes[2]);
        }

        String numeroConstruido = proveedor.getRuc()
                + "-" + serieNormalizada
                + "-" + correlativoNormalizado;

        return new DatosFactura(numeroConstruido, serieNormalizada, correlativoNormalizado);
    }

    private record DatosFactura(
            String numeroFactura,
            String serie,
            String correlativo
    ) { }
}
