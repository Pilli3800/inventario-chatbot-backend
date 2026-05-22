package com.pilli3800.inventario.data.dto.response;

import com.pilli3800.inventario.data.models.comprobantedevolucion.ComprobanteDevolucionFirma;
import com.pilli3800.inventario.data.models.enums.RolComprobanteDevolucionFirma;

import java.time.LocalDateTime;

public record ComprobanteDevolucionFirmaDto(
        RolComprobanteDevolucionFirma rolFirma,
        String codigoUsuario,
        String nombreUsuario,
        String dni,
        LocalDateTime fechaFirma
) {
    public static ComprobanteDevolucionFirmaDto from(ComprobanteDevolucionFirma firma) {
        return new ComprobanteDevolucionFirmaDto(
                firma.getRolFirma(),
                firma.getCodigoUsuarioSnapshot(),
                firma.getNombreUsuarioSnapshot(),
                firma.getDniSnapshot(),
                firma.getFechaFirma()
        );
    }
}
