package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeVisitasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SuspensionVisitasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface InformeVisitasService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeVisitasDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los informes de visitas.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<InformeVisitasDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeVisitasDTO>> obtenerInformesVisitasPaginado(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SuspensionVisitasDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todas las suspensiones de visitas.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<SuspensionVisitasDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SuspensionVisitasDTO>> obtenerSuspensionVisitasPaginado(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeVisitasDTO si el informe 
     * se creó o editó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto InformeVisitasDTO a crear o editar.
     * @param nemonicoMenu nemonico del menú desde donde se hace la petición.
     *
     * @return RespuestaPorDefectoAuditoria<InformeVisitasDTO>
     */
    RespuestaPorDefectoAuditoria<InformeVisitasDTO> crearInformeVisitas(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SuspensionVisitasDTO si la suspensión 
     * se creó o editó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto SuspensionVisitasPorPersonaDTO a crear o editar.
     * @param nemonicoMenu nemonico del menú desde donde se hace la petición.
     *
     * @return RespuestaPorDefectoAuditoria<SuspensionVisitasDTO>
     */
    RespuestaPorDefectoAuditoria<SuspensionVisitasDTO> crearSuspensionVisitas(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu);
    
    /**
     * Elimina un informe de visitas del sistema.
     * La eliminación es lógica, se marca el campo removido como verdadero.
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado objeto InformeVisitasDTO con los datos del informe a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean> true si se eliminó correctamente
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarInformeVisitas(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una suspensión de visitas del sistema.
     * La eliminación es lógica, se marca el campo removido como verdadero.
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado objeto SuspensionVisitasDTO con los datos de la suspensión a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean> true si se eliminó correctamente
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSuspensionVisitas(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}