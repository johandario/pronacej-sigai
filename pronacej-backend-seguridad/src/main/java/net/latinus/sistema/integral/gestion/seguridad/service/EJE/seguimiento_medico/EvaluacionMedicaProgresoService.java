package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaProgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EvaluacionMedicaProgresoService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la evaluacion medica progreso encontrada encontrada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaMedica que contiene la evaluacion medica progreso
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> getEvaluacionMedicaProgresoByIdTokenId (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria PaginacionResponse con las evaluaciones medicas progreso encontradas
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaMedica que contiene las evaluaciones medicas progreso
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaProgresoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaProgresoDTO>> getEvaluacionMedicaProgresoByIdFichaMedica (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la evaluacion medica progreso creada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto EvaluacionMedicaProgreso a crear
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> postEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la evaluacion medica progreso actualizada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto EvaluacionMedicaProgreso a editar
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> updateEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria booleano indicando que la evaluacion fue eliminada con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la EvaluacionMedicaProgreso a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
