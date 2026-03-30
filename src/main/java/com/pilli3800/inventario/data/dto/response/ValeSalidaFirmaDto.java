package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.enums.RolValeSalidaFirma;
import com.pilli3800.inventario.data.models.valesalida.ValeSalidaFirma;

import java.time.LocalDateTime;

public record ValeSalidaFirmaDto(
        RolValeSalidaFirma rolFirma,
        String codigoUsuario,
        String nombreUsuario,
        String dni,
        LocalDateTime fechaFirma
) {
    public static ValeSalidaFirmaDto from(ValeSalidaFirma firma) {
        return new ValeSalidaFirmaDto(
                firma.getRolFirma(),
                firma.getCodigoUsuarioSnapshot(),
                firma.getNombreUsuarioSnapshot(),
                firma.getDniSnapshot(),
                firma.getFechaFirma()
        );
    }
}
