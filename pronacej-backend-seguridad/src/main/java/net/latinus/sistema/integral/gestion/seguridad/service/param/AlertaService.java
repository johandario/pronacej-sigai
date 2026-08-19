package net.latinus.sistema.integral.gestion.seguridad.service.param;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AlertaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface AlertaService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data AlertaDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<AlertaDTO>> obtenerListaAlertas(HttpServletRequest httpServletRequest,
                                                                                    BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data AlertaDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<List<AlertaDTO>> obtenerAlertas(HttpServletRequest httpServletRequest,
                                                                 BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> crearAlerta(HttpServletRequest httpServletRequest,
                                                      BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarAlerta(HttpServletRequest httpServletRequest,
                                                           BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> removerAlerta(HttpServletRequest httpServletRequest,
                                                        BodyEncriptado bodyEncriptado);
}
