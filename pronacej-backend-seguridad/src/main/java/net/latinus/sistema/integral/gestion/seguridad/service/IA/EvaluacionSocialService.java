package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EvaluacionSocialService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionSocialDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las evaluaciones Sociales.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionSocialDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialDTO>> obtenerEvaluacionesSociales(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionSocialDTO si la evaluación social se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto EvaluacionSocialDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionSocialDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionSocialDTO> crearEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una evaluación social del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la evaluación social a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
