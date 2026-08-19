package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.SeguimientoConductualDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.SeguimientoPsicologicoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface SeguimientoService {

    // region Seguimiento Psicologico

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado request peticion.
     * @return RespuestaPorDefectoAuditoria<SeguimientoPsicologicoDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoPsicologicoDTO>> obtenerSeguimientosPsicologicos(HttpServletRequest httpServletRequest,
                                                                                                                BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoPsicologicoDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> crearSeguimientoPsicologico(HttpServletRequest httpServletRequest,
                                                                           BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoPsicologicoDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarSeguimientoPsicologico(HttpServletRequest httpServletRequest,
                                                                           BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoPsicologicoDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimientoPsicologico(HttpServletRequest httpServletRequest,
                                                                         BodyEncriptado bodyEncriptado);

    // endregion

    // region Seguimiento Conductual

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado request peticion.
     * @return RespuestaPorDefectoAuditoria<SeguimientoConductualDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoConductualDTO>> obtenerSeguimientosConductuales(HttpServletRequest httpServletRequest,
                                                                                                               BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoConductualDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> crearSeguimientoConductual(HttpServletRequest httpServletRequest,
                                                                      BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoConductualDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarSeguimientoConductual(HttpServletRequest httpServletRequest,
                                                                           BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoConductualDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimientoConductual(HttpServletRequest httpServletRequest,
                                                                         BodyEncriptado bodyEncriptado);
    // endregion
}
