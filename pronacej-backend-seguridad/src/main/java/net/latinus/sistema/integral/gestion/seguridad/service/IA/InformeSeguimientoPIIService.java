package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeSeguimientoPIIDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface InformeSeguimientoPIIService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeSeguimientoPIIDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los informes de seguimiento.
     *
     * @return RespuestaPorDefectoAuditoria<InformeSeguimientoPIIDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeSeguimientoPIIDTO>> obtenerInformesSeguimientoPaginado(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeSeguimientoPIIDTO si el informe de seguimiento se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto InformeSeguimientoPIIDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<InformeSeguimientoPIIDTO>
     */
    RespuestaPorDefectoAuditoria<InformeSeguimientoPIIDTO> crearInformeSeguimiento(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un informe de seguimiento del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del informe de seguimiento a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarInformeSeguimiento(
            HttpServletRequest httpServletRequest, 
            BodyEncriptado bodyEncriptado);
}