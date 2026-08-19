package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CondHistViolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionConductualDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituPersCaraPersDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EvaluacionConductualService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionConductualDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las evaluaciones conductuales.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionConductualDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionConductualDTO>> obtenerEvaluacionesConductuales(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SituPersCaraPersDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las situaciones personales y características personales.
     *
     * @return RespuestaPorDefectoAuditoria<SituPersCaraPersDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SituPersCaraPersDTO>> obtenerSituPersCaraPers(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data CondHistViolDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las conductas e históricos de violencia.
     *
     * @return RespuestaPorDefectoAuditoria<CondHistViolDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CondHistViolDTO>> obtenerCondHistViol(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionConductualDTO si la evaluación conductual se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto EvaluacionConductualDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionConductualDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionConductualDTO> crearEvaluacionConductual(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una evaluación conductual del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la evaluación conductual a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionConductual(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una situación personal del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la situación personal a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSituPersCaraPers(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una conducta e histórico de violencias del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la conducta e histórico de violencias a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarCondHistViol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
}
