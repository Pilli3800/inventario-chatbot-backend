package com.pilli3800.inventario.service;

import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardDto;
import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardFechaDto;
import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardResumenDto;
import com.pilli3800.inventario.data.dto.response.ia.IaChatDashboardTopUsuarioDto;
import com.pilli3800.inventario.data.models.chat.IaChatMetrica;
import com.pilli3800.inventario.repository.IaChatMetricaRepository;
import com.pilli3800.inventario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IaChatMetricaService {

    private static final LocalDateTime FECHA_MINIMA = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FECHA_MAXIMA = LocalDateTime.of(3000, 1, 1, 0, 0);

    private final IaChatMetricaRepository iaChatMetricaRepository;
    private final UserRepository userRepository;

    public void registrarConsulta(
            String sessionId,
            String usuario,
            String mensaje,
            String respuesta,
            boolean exitosa,
            String mensajeError
    ) {
        IaChatMetrica metrica = new IaChatMetrica();
        metrica.setSessionId(sessionId);
        metrica.setUsuario(usuario);
        metrica.setFechaConsulta(LocalDateTime.now());
        metrica.setLongitudMensaje(longitud(mensaje));
        metrica.setLongitudRespuesta(longitud(respuesta));
        metrica.setExitosa(exitosa);
        metrica.setMensajeError(recortar(mensajeError));

        iaChatMetricaRepository.save(metrica);
    }

    public IaChatDashboardDto getDashboard(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String usuario
    ) {
        LocalDateTime fechaDesdeFiltro = fechaDesde != null
                ? fechaDesde.atStartOfDay()
                : FECHA_MINIMA;
        LocalDateTime fechaHastaFiltro = fechaHasta != null
                ? fechaHasta.atTime(23, 59, 59)
                : FECHA_MAXIMA;
        String usuarioFiltro = usuario != null && !usuario.isBlank()
                ? usuario
                : null;

        IaChatDashboardResumenDto resumen = iaChatMetricaRepository.obtenerResumenDashboard(
                fechaDesdeFiltro,
                fechaHastaFiltro,
                usuarioFiltro
        );

        Long totalUsuariosActivos = userRepository.countByEnabledTrue();
        Long consultasHoy = contarConsultasDesde(LocalDate.now().atStartOfDay(), usuarioFiltro);
        Long consultasUltimos7Dias = contarConsultasDesde(LocalDate.now().minusDays(6).atStartOfDay(), usuarioFiltro);
        Long consultasUltimos30Dias = contarConsultasDesde(LocalDate.now().minusDays(29).atStartOfDay(), usuarioFiltro);

        List<IaChatDashboardFechaDto> porFecha = iaChatMetricaRepository.obtenerPorFechaDashboard(
                fechaDesdeFiltro,
                fechaHastaFiltro,
                usuarioFiltro
        );
        List<IaChatDashboardTopUsuarioDto> topUsuarios = iaChatMetricaRepository.obtenerTopUsuariosDashboard(
                fechaDesdeFiltro,
                fechaHastaFiltro,
                usuarioFiltro
        );

        return new IaChatDashboardDto(
                resumen.totalConsultas(),
                valor(resumen.consultasExitosas()),
                valor(resumen.consultasFallidas()),
                resumen.totalSesiones(),
                resumen.usuariosQueUsaronIA(),
                totalUsuariosActivos,
                porcentaje(resumen.usuariosQueUsaronIA(), totalUsuariosActivos),
                promedio(resumen.totalConsultas(), resumen.totalSesiones()),
                consultasHoy,
                consultasUltimos7Dias,
                consultasUltimos30Dias,
                porFecha,
                topUsuarios
        );
    }

    private Long contarConsultasDesde(LocalDateTime fechaDesde, String usuario) {
        return iaChatMetricaRepository.contarConsultas(
                fechaDesde,
                LocalDateTime.now(),
                usuario
        );
    }

    private Integer longitud(String valor) {
        return valor != null ? valor.length() : 0;
    }

    private String recortar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.length() <= 500 ? valor : valor.substring(0, 500);
    }

    private Long valor(Long valor) {
        return valor != null ? valor : 0L;
    }

    private Double porcentaje(Long parte, Long total) {
        if (total == null || total == 0) {
            return 0.0;
        }
        return Math.round((valor(parte) * 10000.0) / total) / 100.0;
    }

    private Double promedio(Long totalConsultas, Long totalSesiones) {
        if (totalSesiones == null || totalSesiones == 0) {
            return 0.0;
        }
        return Math.round((valor(totalConsultas) * 100.0) / totalSesiones) / 100.0;
    }
}
