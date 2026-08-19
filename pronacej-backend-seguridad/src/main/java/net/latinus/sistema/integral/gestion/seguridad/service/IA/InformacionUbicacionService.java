package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface InformacionUbicacionService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformacionUbicacionDTO si la persona relacionada se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto de la informacion ubicacion dto de la persona relacionada.
     *
     * @return RespuestaPorDefectoAuditoria<InformacionUbicacionDTO>
     */
    RespuestaPorDefectoAuditoria<InformacionUbicacionDTO> crearInformacionUbicacion(HttpServletRequest httpServletRequest,
                                                                                  BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformacionUbicacionDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado idPersonaRelacionada para obtener las direcciones asociadas.
     *
     * @return RespuestaPorDefectoAuditoria<DireccionPersonaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformacionUbicacionDTO>> obtenerInformacionUbicaciones(HttpServletRequest httpServletRequest,
                                                                                                         BodyEncriptado bodyEncriptado);

    /**
     * Elimina una direccion relacionada con la persona
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado InformacionUbicacionDTO datos de la informacion ubicacion a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarInformacionUbicacion(HttpServletRequest httpServletRequest,
                                                                       BodyEncriptado bodyEncriptado);
}
