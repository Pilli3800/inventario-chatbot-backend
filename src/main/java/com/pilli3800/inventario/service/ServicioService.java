package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.servicios.ServicioCreateRequest;
import com.pilli3800.inventario.data.dto.request.servicios.ServicioSearchRequest;
import com.pilli3800.inventario.data.dto.request.servicios.ServicioUpdateRequest;
import com.pilli3800.inventario.data.dto.response.ServicioDto;
import com.pilli3800.inventario.data.models.Servicio;
import com.pilli3800.inventario.repository.ServicioRepository;
import com.pilli3800.inventario.specifications.ServicioSpecifications;
import com.pilli3800.inventario.validator.ServicioUpdateValidator;
import com.pilli3800.inventario.validator.ServicioValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioValidator servicioValidator;
    private final ServicioUpdateValidator servicioUpdateValidator;

    public ServicioDto getServicio(String codigo) {
        return servicioRepository.findByCodigo(codigo).map(ServicioDto::from).orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
    }

    public Page<ServicioDto> getServicios(ServicioSearchRequest request, Pageable pageable) {

        Specification<Servicio> spec =
                ServicioSpecifications.search(request);

        return servicioRepository
                .findAll(spec, pageable)
                .map(ServicioDto::from);
    }

    public ServicioDto createServicio(ServicioCreateRequest request) {
        servicioValidator.validateCreate(request);

        Servicio servicio = new Servicio();
        servicio.setCodigo(request.codigoServicio());
        servicio.setNombre(request.nombreServicio());
        servicio.setDescripcion(request.descripcionServicio());
        servicio.setEnabled(true);

        return ServicioDto.from(
                servicioRepository.save(servicio)
        );
    }

    @Transactional
    public ServicioDto updateServicio(String codigo, ServicioUpdateRequest request) {

        servicioUpdateValidator.validate(codigo);

        Servicio servicio = servicioRepository
                .findByCodigo(codigo)
                .get();

        if (request.nombreServicio() != null) {
            servicio.setNombre(request.nombreServicio());
        }

        if (request.descripcionServicio() != null) {
            servicio.setDescripcion(request.descripcionServicio());
        }

        if (request.enabled() != null) {
            servicio.setEnabled(request.enabled());
        }

        return ServicioDto.from(servicio);
    }

    public void disableServicio(String codigo) {
        setEnabled(codigo, false);
    }

    public void enableServicio(String codigo) {
        setEnabled(codigo, true);
    }

    private void setEnabled(String codigo, boolean enabled) {

        Servicio servicio = servicioRepository.findByCodigo(codigo).orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setEnabled(enabled);
        servicioRepository.save(servicio);
    }
}