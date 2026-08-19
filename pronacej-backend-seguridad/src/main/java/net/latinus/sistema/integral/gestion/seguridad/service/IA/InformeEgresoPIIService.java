package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeEgresoPIIDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface InformeEgresoPIIService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeEgresoPIIDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los informes de egreso.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<InformeEgresoPIIDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeEgresoPIIDTO>> obtenerInformesEgreso(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeEgresoPIIDTO si el informe se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto InformeEgresoPIIDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<InformeEgresoPIIDTO>
     */
    RespuestaPorDefectoAuditoria<InformeEgresoPIIDTO> crearInformeEgreso(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un informe de egreso del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado InformeEgresoPIIDTO datos del informe a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarInformeEgreso(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}