package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EstadoNutricionalDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EstadoNutricionalService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la ficha médica encontrada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaIdentificacion que contiene la ficha medica
     *
     * @return RespuestaPorDefectoAuditoria<EstadoNutricionalDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EstadoNutricionalDTO>> getEstadoNutricionalByIdEvaluacionMedica (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con EstadoNutricional creado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto EstadoNutricional a crear
     *
     * @return RespuestaPorDefectoAuditoria<EstadoNutricionalDTO>
     */
    RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> postEstadoNutricional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con EstadoNutricional actualizado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto EstadoNutricional a editar
     *
     * @return RespuestaPorDefectoAuditoria<EstadoNutricionalDTO>
     */
    RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> updateEstadoNutricional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria booleano indicando que EstadoNutricional fue eliminado con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la EstadoNutricional a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<<RespuestaPorDefectoAuditoria<Boolean>>
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteEstadoNutricional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
