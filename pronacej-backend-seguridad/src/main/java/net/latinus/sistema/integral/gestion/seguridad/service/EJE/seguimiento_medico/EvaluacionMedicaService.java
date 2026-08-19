package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EvaluacionMedicaService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la evaluacion medica encontrada encontrada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaMedica que contiene la evaluacion medica
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO> getEvaluacionMedicaByIdTokenId (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria PaginacionResponse con las evaluaciones medicsa encontradas
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaMedica que contiene las evaluaciones medicas
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaDTO>> getEvaluacionMedicaByIdFichaMedica (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la evaluacion medica creada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto EvaluacionMedica a crear
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO> postEvaluacionMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la evaluacion medica actualizada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto EvaluacionMedica a editar
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO> updateEvaluacionMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria booleano indicando que la evaluacion fue eliminada con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la EvaluacionMedica a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteEvaluacionMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
