package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.DiagnosticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface DiagnosticoService {
    /**
     * Devuelve un objeto paginacion response con RespuestaPorDefectoAuditoria con los diagnostico encontrado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la EvaluacionMedica que contiene el diagnostico
     *
     * @return RespuestaPorDefectoAuditoria<DiagnosticoDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DiagnosticoDTO>> getDiagnosticoByIdEvaluacionMedica (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el diagnostico creado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto Diagnostico a crear
     *
     * @return RespuestaPorDefectoAuditoria<DiagnosticoDTO>
     */
    RespuestaPorDefectoAuditoria<DiagnosticoDTO> postDiagnostico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el diagnostico actualizado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto Diagnostico a editar
     *
     * @return RespuestaPorDefectoAuditoria<DiagnosticoDTO>
     */
    RespuestaPorDefectoAuditoria<DiagnosticoDTO> updateDiagnostico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria booleano indicando que el diagnostico fue eliminado con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la Diagnostico a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<<RespuestaPorDefectoAuditoria<Boolean>>
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteDiagnostico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
