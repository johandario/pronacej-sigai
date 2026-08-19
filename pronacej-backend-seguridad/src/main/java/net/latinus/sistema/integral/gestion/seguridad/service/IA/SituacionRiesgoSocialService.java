package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionRiesgoSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface SituacionRiesgoSocialService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SituacionRiesgoSocialDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las situaciones de riesgo social.
     *
     * @return RespuestaPorDefectoAuditoria<SituacionRiesgoSocialDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionRiesgoSocialDTO>> obtenerSituacionesRiesgoSocial(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SituacionRiesgoSocialDTO si la situación de riesgo social se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto SituacionRiesgoSocialDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<SituacionRiesgoSocialDTO>
     */
    RespuestaPorDefectoAuditoria<SituacionRiesgoSocialDTO> crearSituacionRiesgoSocial(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una evaluación social del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la situación de riesgo social a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSituacionRiesgoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
