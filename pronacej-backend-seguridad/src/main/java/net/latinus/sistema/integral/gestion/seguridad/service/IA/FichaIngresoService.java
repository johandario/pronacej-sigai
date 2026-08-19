package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIngresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;

public interface FichaIngresoService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIngresoDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las fichas de ingreso.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIngresoDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIngresoDTO>> obtenerFichasIngreso(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIngresoDTO si la ficha de ingreso se creo con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto FichaIngresoDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIngresoDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIngresoDTO> crearFichaIngreso(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una ficha de ingreso del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la ficha de ingreso a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarFichaIngreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener última ficha de ingreso válida por token de ficha de identificación
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado contiene el tokenIdentificador de la ficha de identificación
     * @return RespuestaPorDefectoAuditoria<FichaIngresoDTO> objeto que cumple con ese criterio
     */
    RespuestaPorDefectoAuditoria<FichaIngresoDTO> obtenerUltimoIngresoValidoPorTokenFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO si la ficha se actualizo con exito
     *
     * @param httpServletRequest request peticion.
     * @param FichaIdentificacionDTO objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> actualizarFicha(HttpServletRequest httpServletRequest,
                                                                   FichaIdentificacionDTO fichaIdentificacionDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO si la empresa se removio con exito
     *
     * @param httpServletRequest request peticion.
     * @param FichaIdentificacionDTO objeto ficha identificacion dto.
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> removerFicha(HttpServletRequest httpServletRequest,
                                                                FichaIdentificacionDTO fichaIdentificacionDTO);



}
