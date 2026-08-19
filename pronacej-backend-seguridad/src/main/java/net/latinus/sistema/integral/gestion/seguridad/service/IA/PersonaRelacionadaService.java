package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface PersonaRelacionadaService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIngresoDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las fichas de ingreso.
     *
     * @return RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> obtenerPersonaRelacionada(HttpServletRequest httpServletRequest,
                                                                                           BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PersonaRelacionadaDTO si la persona relacionada se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param personaRelacionadaDTO objeto persona relacionada dto.
     *
     * @return RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO>
     */
    public RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> crearPersonaRelacionada(HttpServletRequest httpServletRequest,
                                                                                       PersonaRelacionadaDTO personaRelacionadaDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PersonaRelacionadaDTO si la persona relacionada se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado id persona relacionada.
     *
     * @return RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO>
     */
    RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> obtenerPersonaRelacionadaPorToken(HttpServletRequest httpServletRequest,
                                                                                       BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PersonaRelacionadaDTO Paginada filtrada por tokenIdentificadorEvaluacionSocial
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las personas relacionadas.
     *
     * @return RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> obtenerPersonasRelacionadasPorTokenIdentificadorEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                                                       BodyEncriptado bodyEncriptado);

    /**
     * Elimina una persona relacionada con la ficha identificacion del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la persona relacionada a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarPersonaRelacionada(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PersonaRelacionadaDTO si la persona relacionada se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param DireccionPersonaDTO objeto direciccion persona relacionada dto.
     *
     * @return RespuestaPorDefectoAuditoria<DireccionPersonaDTO>
     */
    RespuestaPorDefectoAuditoria<DireccionPersonaDTO> crearDireccionPersona(HttpServletRequest httpServletRequest,
                                                                              DireccionPersonaDTO DireccionPersonaDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data DireccionPersonaDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado idPersonaRelacionada para obtener las direcciones asociadas.
     *
     * @return RespuestaPorDefectoAuditoria<DireccionPersonaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DireccionPersonaDTO>> obtenerDireccionesRelacionadas(HttpServletRequest httpServletRequest,
                                                                                                      BodyEncriptado bodyEncriptado);

    /**
     * Elimina una direccion relacionada con la persona
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la persona relacionada a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarDireccionRelacionada(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una direccion relacionada con la persona
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la persona relacionada a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarPersonaRelacionadaPorSituacionEconomicaSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PersonaRelacionadaDTO si la persona relacionada se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param personaRelacionadaDTO objeto persona relacionada dto.
     *
     * @return RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO>
     */
    public RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> editarPersonaRelacionadaEnfermo(HttpServletRequest httpServletRequest,
                                                                                       PersonaRelacionadaDTO personaRelacionadaDTO);
    /** Devuelve una lista de PersonaRelacionadaDTO por el id de la ficha (adolescente)
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado id persona relacionada.
     *
     * @return RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>>
     */
    RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> obtenerPersonasRelacionadasPorIdFicha(HttpServletRequest httpServletRequest,
                                                                                                BodyEncriptado bodyEncriptado);

    /** Devuelve una lista de PersonaRelacionadaDTO por el id de la ficha (adolescente)
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado id persona relacionada.
     *
     * @return RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>>
     */
    RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> obtenerPersonasRelacionadasPorTokenIdenficadorFicha(HttpServletRequest httpServletRequest,
                                                                                                    BodyEncriptado bodyEncriptado);
    
    /**
     * Busca personas relacionadas por número de documento
     *
     * @param httpServletRequest Petición HTTP
     * @param bodyEncriptado Cuerpo encriptado con el número de documento
     * @return RespuestaPorDefectoAuditoria con la lista de personas relacionadas encontradas
     */
    RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> buscarPersonaRelacionadaPorNumeroDocumento(
        HttpServletRequest httpServletRequest, 
        BodyEncriptado bodyEncriptado
    );
}
