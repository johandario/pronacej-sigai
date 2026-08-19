package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.AuditoriaAccionesSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.Date;

public interface AuditoriaAccionesSistemaService {

    /**
     * Guarda una accion del sistema la descripcion de la auditoria esta en el mensaje de la respuesta,
     * el token de la empresa se asume que se envia en la respuesta
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param jsonRequest String json request.
     * @param respuesta RespuestaPorDefectoAuditoria<T> Objeto respueta del sistema
     * @param fechaInicio Date fecha que inicio la acción
     * @param nemonicoAccion string nemonico de la accion del usuario
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    <T> RespuestaPorDefectoAuditoria<Boolean> guardarAccionRequestEncriptado(HttpServletRequest httpServletRequest,
                                                                         String jsonRequest,
                                                                         RespuestaPorDefectoAuditoria<T> respuesta,
                                                                         Date fechaInicio, String nemonicoAccion);


    /**
     * Guarda una accion del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado body encriptado correspondiente a PaginacionAuditoriasAccionesRequest.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<AuditoriaAccionesSistemaDTO>> obtenerPorFiltros(HttpServletRequest httpServletRequest,
                                                                                                    BodyEncriptado bodyEncriptado);
}
