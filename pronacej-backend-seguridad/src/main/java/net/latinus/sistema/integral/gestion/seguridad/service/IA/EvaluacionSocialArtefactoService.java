package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialArtefactoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EvaluacionSocialArtefactoService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionSocialArtefactoDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los artefactos pertenenecientes a una evaluación social.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionSocialArtefactoDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialArtefactoDTO>> obtenerArtefactosPorEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionSocialArtefactoDTO si el artefacto perteneciente a una evaluación social se creo con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto EvaluacionSocialArtefactoDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionSocialArtefactoDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionSocialArtefactoDTO> crearArtefactoPorEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un artefacto perteneciente a una evaluación social del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del artefacto perteneciente a la  evaluación social a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarArtefactoPorEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
