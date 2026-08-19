package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface FichaMedicaService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con todas las fichas medicas
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene la información para la paginación de las fichas
     *
     * @return RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<FichaMedicaDTO>>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaDTO>> getFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la ficha médica encontrada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaIdentificacion que contiene la ficha medica
     *
     * @return RespuestaPorDefectoAuditoria<RespuestaPorDefectoAuditoria<FichaMedicaDTO>>
     */
    RespuestaPorDefectoAuditoria<FichaMedicaDTO> getFichaMedicaByIdFichaIdentificacion (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la ficha médica creada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto FichaMedica a crear
     *
     * @return RespuestaPorDefectoAuditoria<RespuestaPorDefectoAuditoria<FichaMedicaDTO>>
     */
    RespuestaPorDefectoAuditoria<FichaMedicaDTO> postFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la ficha médica actualizada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto FichaMedica a editar
     *
     * @return RespuestaPorDefectoAuditoria<RespuestaPorDefectoAuditoria<FichaMedicaDTO>>
     */
    RespuestaPorDefectoAuditoria<FichaMedicaDTO> updateFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria booleano indicando que la ficha fue eliminada con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el tokenID de la FichaMedica a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<<RespuestaPorDefectoAuditoria<Boolean>>
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
