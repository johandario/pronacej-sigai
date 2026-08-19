package net.latinus.sistema.integral.gestion.seguridad.service.dashboard;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.DashboardCentroDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.DashboardEstadisticasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.EstadisticaItemDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.DashboardRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.dashboard.DashboardRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final JerarquiaRepository jerarquiaRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CENTROS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public RespuestaPorDefectoAuditoria<List<DashboardCentroDTO>> obtenerCentros(
            HttpServletRequest httpServletRequest) {

        RespuestaPorDefectoAuditoria<List<DashboardCentroDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            List<Jerarquia> centros = jerarquiaRepository.obtenerCentrosDashboard(empresa.getIdEmpresa());

            List<DashboardCentroDTO> centrosDTO = centros.stream()
                    .map(j -> {
                        DashboardCentroDTO dto = new DashboardCentroDTO();
                        dto.setTokenIdentificador(j.getTokenIdentificador());
                        dto.setNombre(j.getNombre());
                        dto.setNemonicoPadre(
                                j.getJerarquiaPadre() != null ? j.getJerarquiaPadre().getNemonico() : null);
                        dto.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
                        return dto;
                    })
                    .collect(Collectors.toList());

            df.llenarRespuestaExitosa(
                    "Se encontraron " + centrosDTO.size() + " centros disponibles",
                    centrosDTO,
                    "Consulta de centros disponibles para dashboard de la empresa " + empresa.getNombre()
            );

        } catch (Exception e) {
            df.llenarConDatosDeException(e);
        }

        return df;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ESTADÍSTICAS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public RespuestaPorDefectoAuditoria<DashboardEstadisticasDTO> obtenerEstadisticas(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<DashboardEstadisticasDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // 1. Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // 2. Desencriptar body
            RespuestaPorDefectoAuditoria<String> df22 =
                    bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);

            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            // 3. Parsear request
            DashboardRequest request = new Gson().fromJson(df22.getData(), DashboardRequest.class);

            Long empresaId = empresa.getIdEmpresa();
            String tokenCentro = resolverTokenCentro(request.getTokenCentro());

            // 4. Ejecutar las 4 consultas agregadas y construir el DTO respuesta
            DashboardEstadisticasDTO estadisticas = new DashboardEstadisticasDTO();

            estadisticas.setPorDelito(
                    mapearResultados(dashboardRepository.countPorDelito(empresaId, tokenCentro)));

            estadisticas.setPorEdad(
                    mapearResultados(dashboardRepository.countPorEdad(empresaId, tokenCentro)));

            estadisticas.setPorSexo(
                    mapearResultados(dashboardRepository.countPorSexo(empresaId, tokenCentro)));

            estadisticas.setPorNacionalidad(
                    mapearResultados(dashboardRepository.countPorNacionalidad(empresaId, tokenCentro)));

            estadisticas.setPorDepartamento(
                    mapearResultados(dashboardRepository.countPorDepartamento(empresaId, tokenCentro)));

            estadisticas.setPorDiasInternacion(
                    mapearResultados(dashboardRepository.countPorDiasInternacion(empresaId, tokenCentro)));

            estadisticas.setPorTipoEnfermedad(
                    mapearResultados(dashboardRepository.countPorTipoEnfermedad(empresaId, tokenCentro)));

            estadisticas.setPorGradoInstruccion(
                    mapearResultados(dashboardRepository.countPorGradoInstruccion(empresaId, tokenCentro)));

            estadisticas.setPorNumeroHijos(
                    mapearResultados(dashboardRepository.countPorNumeroHijos(empresaId, tokenCentro)));

            estadisticas.setPorNumeroCentros(
                    mapearResultados(dashboardRepository.countPorNumeroCentros(empresaId)));

            // 5. Construir mensajes
            String centroDesc = tokenCentro != null
                    ? "centro con token " + tokenCentro
                    : "todos los centros";

            df.llenarRespuestaExitosa(
                    "Estadísticas del dashboard obtenidas exitosamente",
                    estadisticas,
                    "Se consultaron las estadísticas del dashboard para " + centroDesc
                            + " de la empresa " + empresa.getNombre()
            );

        } catch (Exception e) {
            df.llenarConDatosDeException(e);
        }

        return df;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTODOS PRIVADOS DE APOYO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convierte cada fila Object[] devuelta por las consultas en un {@link EstadisticaItemDTO}.
     * Convenio: Object[0] = etiqueta (String o Number), Object[1] = cantidad (Number).
     * Reutilizable para cualquier nueva métrica agregada al repositorio.
     */
    private List<EstadisticaItemDTO> mapearResultados(List<Object[]> resultados) {
        return resultados.stream()
                .map(obj -> new EstadisticaItemDTO(
                        obj[0] != null ? obj[0].toString() : "Sin información",
                        obj[1] != null ? ((Number) obj[1]).longValue() : 0L
                ))
                .collect(Collectors.toList());
    }

    /**
     * Normaliza el token de centro: devuelve {@code null} si está vacío,
     * para que las queries JPQL/SQL apliquen la condición {@code :tokenCentro IS NULL}
     * y omitan el filtro de centro (= todos los centros de la empresa).
     */
    private String resolverTokenCentro(String tokenCentro) {
        return (tokenCentro == null || tokenCentro.trim().isEmpty()) ? null : tokenCentro.trim();
    }
}

