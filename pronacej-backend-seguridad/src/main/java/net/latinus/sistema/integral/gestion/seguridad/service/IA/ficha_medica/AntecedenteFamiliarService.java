package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.AntecedenteFamiliarDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface AntecedenteFamiliarService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la paginacion de antecedente familiar
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene la información para la paginación y el token id de la ficha medica
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<AntecedenteFamiliarDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<AntecedenteFamiliarDTO>> getAntecedenteFamiliarByTokenIdFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el antecedente familiar creado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto antecedente familiar a crear
     *
     * @return RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> devuelve el ingreso a centro creado
     */
    RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> postAntecedenteFamiliar(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el antecedente familiar editado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto antecedente familiar a editar
     *
     * @return RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> devuelve el ingreso a centro editado
     */
    RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> updateAntecedenteFamiliar(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con un Booleano que indica si el antecedente familiar ha sido eliminado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el token identificador del antecedente familiar a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<Boolean> devuelve si el centro ha sido eliminado
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteAntecedenteFamiliar(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
