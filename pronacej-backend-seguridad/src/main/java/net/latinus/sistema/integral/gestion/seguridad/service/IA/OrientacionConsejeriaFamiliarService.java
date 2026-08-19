package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.OrientacionConsejeriaFamiliarDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface OrientacionConsejeriaFamiliarService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data OrientacionConsejeriaFamiliarDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todas las orientaciones y consejerías familiares.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>> obtenerOrientacionesConsejerias(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data OrientacionConsejeriaFamiliarDTO si la orientación/consejería 
     * se creó o editó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto OrientacionConsejeriaFamiliarDTO a crear o editar.
     *
     * @return RespuestaPorDefectoAuditoria<OrientacionConsejeriaFamiliarDTO>
     */
    RespuestaPorDefectoAuditoria<OrientacionConsejeriaFamiliarDTO> crearOrientacionConsejeria(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina una orientación/consejería familiar del sistema.
     * La eliminación es lógica, se marca el campo removido como verdadero.
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado objeto OrientacionConsejeriaFamiliarDTO con los datos de la orientación/consejería a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean> true si se eliminó correctamente
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarOrientacionConsejeria(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}