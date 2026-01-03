package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.SedeUpdateRequest;
import com.pilli3800.inventario.data.dto.response.SedeDto;
import com.pilli3800.inventario.data.models.Sede;
import com.pilli3800.inventario.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;

    public List<SedeDto> getSedes() {
        return sedeRepository.findAll().stream().map(SedeDto::from).toList();
    }

    public SedeDto getSede(String codigoSede) {
        return sedeRepository.findByCodigo(codigoSede).map(SedeDto::from).orElseThrow(() -> new RuntimeException("Sede no encontrada"));
    }

    public List<SedeDto> getSedesAtivas() {
        return sedeRepository.findAllByEnabledTrue().stream().map(SedeDto::from).toList();
    }

    private void setSedeEnabled(String codigoSede, boolean enabled) {
        Sede sede = sedeRepository.findByCodigo(codigoSede)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        sede.setEnabled(enabled);
        sedeRepository.save(sede);
    }

    public void enableSede(String codigoSede) {
        setSedeEnabled(codigoSede, true);
    }

    public void disableSede(String codigoSede) {
        setSedeEnabled(codigoSede, false);
    }

    public SedeDto updateSede(SedeUpdateRequest request, String codigoSede) {
        Sede sede = sedeRepository.findByCodigo(codigoSede)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        sede.setNombre(request.nombre());
        sede.setDescripcion(request.descripcion());
        return SedeDto.from(sedeRepository.save(sede));
    }
}
