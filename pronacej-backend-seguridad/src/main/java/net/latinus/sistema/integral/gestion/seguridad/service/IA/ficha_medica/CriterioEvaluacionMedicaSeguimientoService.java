package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface CriterioEvaluacionMedicaSeguimientoService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con todos los criterios de evaluacion
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene la información para la paginación de los criterios de seguimiento a la ficha medica
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>> getCriteriosDeEvaluacion(HttpServletRequest httpServletRequest,
                                                                                                                               BodyEncriptado bodyEncriptado);

}
