package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaCreateRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaSearchRequest;
import com.pilli3800.inventario.data.dto.request.cuadrilla.CuadrillaUpdateRequest;
import com.pilli3800.inventario.data.dto.response.CuadrillaDto;
import com.pilli3800.inventario.data.models.Cuadrilla;
import com.pilli3800.inventario.data.models.user.User;
import com.pilli3800.inventario.repository.CuadrillaRepository;
import com.pilli3800.inventario.repository.UserRepository;
import com.pilli3800.inventario.specifications.CuadrillaSpecifications;
import com.pilli3800.inventario.validator.CuadrillaUpdateValidator;
import com.pilli3800.inventario.validator.CuadrillaValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuadrillaService {
    private final CuadrillaRepository cuadrillaRepository;
    private final CuadrillaValidator cuadrillaValidator;
    private final UserRepository userRepository;
    private final CuadrillaUpdateValidator cuadrillaUpdateValidator;

    public CuadrillaDto getCuadrilla(String codigoCuadrilla) {
        return cuadrillaRepository.findByCodigoCuadrilla(codigoCuadrilla).map(CuadrillaDto::from).orElseThrow(() -> new RuntimeException("Cuadrilla no encontrada"));
    }

    public Page<CuadrillaDto> getCuadrillas(CuadrillaSearchRequest request, Pageable pageable) {
        Specification<Cuadrilla> spec = CuadrillaSpecifications.search(request);
        Page<Cuadrilla> page = cuadrillaRepository.findAll(spec, pageable);
        return page.map(CuadrillaDto::from);
    }

    public CuadrillaDto createCuadrilla(CuadrillaCreateRequest request) {

        cuadrillaValidator.validate(request);

        Cuadrilla cuadrilla = new Cuadrilla();
        cuadrilla.setCodigoCuadrilla(request.codigoCuadrilla());

        User usuario = userRepository.findByIdentUsuario(request.codigoUsuario()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        cuadrilla.setJefeCuadrilla(usuario);

        return CuadrillaDto.from(cuadrillaRepository.save(cuadrilla));
    }

    public void enableItem(String codigoItem) {
        setCuadrillaEnabled(codigoItem, true);
    }

    public void disableItem(String codigoItem) {
        setCuadrillaEnabled(codigoItem, false);
    }

    private void setCuadrillaEnabled(String codigoCuadrilla, boolean enabled) {
        Cuadrilla cuadrilla = cuadrillaRepository.findByCodigoCuadrilla(codigoCuadrilla)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        cuadrilla.setEnabled(enabled);
        cuadrillaRepository.save(cuadrilla);
    }

    @Transactional
    public CuadrillaDto updateCuadrilla(
            String codigoCuadrilla,
            CuadrillaUpdateRequest request
    ) {

        cuadrillaUpdateValidator.validate(codigoCuadrilla, request);

        Cuadrilla cuadrilla = cuadrillaRepository
                .findByCodigoCuadrilla(codigoCuadrilla)
                .get();

        if (request.codigoUsuario() != null) {
            User nuevoJefe = userRepository
                    .findByIdentUsuario(request.codigoUsuario())
                    .get();

            cuadrilla.setJefeCuadrilla(nuevoJefe);
        }

        if (request.enabled() != null) {
            cuadrilla.setEnabled(request.enabled());
        }

        return CuadrillaDto.from(cuadrilla);
    }

    public byte[] exportCuadrillasToExcel(CuadrillaSearchRequest request) throws IOException {

        Specification<Cuadrilla> spec =
                CuadrillaSpecifications.search(request);

        List<Cuadrilla> cuadrillas =
                cuadrillaRepository.findAll(spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cuadrillas");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);

        Cell h0 = header.createCell(0);
        h0.setCellValue("ID");
        h0.setCellStyle(headerStyle);

        Cell h1 = header.createCell(1);
        h1.setCellValue("Código Cuadrilla");
        h1.setCellStyle(headerStyle);

        Cell h2 = header.createCell(2);
        h2.setCellValue("Usuario Jefe");
        h2.setCellStyle(headerStyle);

        Cell h3 = header.createCell(3);
        h3.setCellValue("Nombre Jefe");
        h3.setCellStyle(headerStyle);

        Cell h4 = header.createCell(4);
        h4.setCellValue("Activo");
        h4.setCellStyle(headerStyle);

        int rowIdx = 1;
        for (Cuadrilla cuadrilla : cuadrillas) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(cuadrilla.getId());
            row.createCell(1).setCellValue(cuadrilla.getCodigoCuadrilla());
            row.createCell(2).setCellValue(
                    cuadrilla.getJefeCuadrilla().getIdentUsuario()
            );
            row.createCell(3).setCellValue(
                    cuadrilla.getJefeCuadrilla().getNombres() + " " +
                            cuadrilla.getJefeCuadrilla().getApellidos()
            );
            row.createCell(4).setCellValue(cuadrilla.isEnabled());
        }

        for (int i = 0; i <= 4; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

}