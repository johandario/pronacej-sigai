package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;

import java.util.List;

public interface FichaIdentificacionService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las fichas de ingreso.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionDTO>> obtenerFichasIdentificacion(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionResumenDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto cifrado que contiene petición de paginación.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionResumenDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionResumenDTO>> obtenerFichasIdentificacionResumido(HttpServletRequest httpServletRequest,
                                                                                                                        BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO si la ficha se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptad objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> crearFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                              BodyEncriptado bodyEncriptad);
    
    /**
     * Elimina una ficha de identificación del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del usuario a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO de la ficha correspondiente al token Identificador
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> obtenerFichaIdentificacionPorTokenIdentificador(HttpServletRequest httpServletRequest,
                                                                    BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO de la ficha correspondiente al token Identificador
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> obtenerFichaIdentificacionPorId(HttpServletRequest httpServletRequest,
                                                                                                         BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO
     *
     * @param httpServletRequest request peticion.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<List<FichaIdentificacionDTO>> obtenerNombresFichas(HttpServletRequest httpServletRequest, String tokenCentro);
    //RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> removerFicha(HttpServletRequest httpServletRequest,
     //                                                           FichaIdentificacionDTO fichaIdentificacionDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO de la ficha correspondiente al token Identificador
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> obtenerFichaIdentificacionPorNumeroDocumento(HttpServletRequest httpServletRequest,
                                                                                                         BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<List<EdadEstadisticaDTO>> obtenerEstadisticasEdades(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> obtenerEstadisticasEstados(HttpServletRequest httpServletRequest);

    RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> obtenerEstadisticasSexo(HttpServletRequest httpServletRequest);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con Boolean de la validación
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> validarIngresoNuevo(HttpServletRequest httpServletRequest,
                                                                                  BodyEncriptado bodyEncriptado);
}
