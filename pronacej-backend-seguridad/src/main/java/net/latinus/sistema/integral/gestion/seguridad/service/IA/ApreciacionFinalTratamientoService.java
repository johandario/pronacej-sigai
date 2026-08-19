package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ApreciacionFinalTratamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ApreciacionFinalTratamientoService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data ApreciacionFinalTratamientoDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todas las apreciaciones finales.
     * @param nemonicoMenu nemonico del menu.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ApreciacionFinalTratamientoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<ApreciacionFinalTratamientoDTO>> obtenerApreciacionesFinalesPaginado(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data ApreciacionFinalTratamientoDTO si la apreciación final se creó/actualizó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto ApreciacionFinalTratamientoDTO a crear/actualizar.
     * @param nemonicoMenu nemonico del menu.
     *
     * @return RespuestaPorDefectoAuditoria<ApreciacionFinalTratamientoDTO>
     */
    RespuestaPorDefectoAuditoria<ApreciacionFinalTratamientoDTO> crearApreciacionFinal(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu);
    
    /**
     * Elimina una apreciación final del sistema (elimina tanto situaciones como factores asociados)
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado ApreciacionFinalTratamientoDTO datos de la apreciación final a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarApreciacionFinal(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una situación específica del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado SituacionActualAdolescenteDTO datos de la situación a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSituacion(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un factor específico del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado FactoresPresentesDTO datos del factor a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarFactor(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}