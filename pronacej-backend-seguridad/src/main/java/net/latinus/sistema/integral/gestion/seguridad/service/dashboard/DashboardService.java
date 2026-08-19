package net.latinus.sistema.integral.gestion.seguridad.service.dashboard;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.DashboardCentroDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.DashboardEstadisticasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface DashboardService {

    /**
     * Obtiene los centros disponibles para el selector del dashboard.
     * Devuelve las Jerarquías hijas cuyo padre tiene nemónico SOA, CJDR o UAPISE,
     * propias de la empresa identificada en el JWT.
     *
     * @param httpServletRequest datos de la petición HTTP (contiene JWT)
     * @return lista de centros disponibles
     */
    RespuestaPorDefectoAuditoria<List<DashboardCentroDTO>> obtenerCentros(
            HttpServletRequest httpServletRequest);

    /**
     * Obtiene el conjunto de estadísticas agregadas para un centro específico
     * (o para todos los centros de la empresa si no se especifica ninguno).
     * Incluye: distribución por edad, sexo, nacionalidad y departamento de domicilio.
     *
     * @param httpServletRequest datos de la petición HTTP (contiene JWT)
     * @param bodyEncriptado     cuerpo cifrado que contiene un {@code DashboardRequest}
     * @return objeto con todas las estadísticas separadas por criterio
     */
    RespuestaPorDefectoAuditoria<DashboardEstadisticasDTO> obtenerEstadisticas(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);
}

