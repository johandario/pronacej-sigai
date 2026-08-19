package net.latinus.sistema.integral.gestion.seguridad.service.salida;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.ReforzamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RelacionEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface RelacionEgresoService {

    /**
     * Devuelve una lista paginada de los adolescentes.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < RelacionEgresoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos RelacionEgresoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<RelacionEgresoDTO>> obtenerAdolescentes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve una lista paginada de los reforzamientos.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < ReforzamientoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ReforzamientoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<ReforzamientoDTO>> obtenerReforzamientos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un reforzamiento.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Token del reofrzamiento.
     * @return RespuestaPorDefectoAuditoria<ReforzamientoDTO>, Devuelve respuesta para auditoria con Reforzamiento
     */
    RespuestaPorDefectoAuditoria<ReforzamientoDTO> obtenerReforzamientoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve RespuestaPorDefectoAuditoria<Boolean>.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ReforzamientoDTO
     * @return RespuestaPorDefectoAuditoria<Boolean>, Devuelve respuesta para auditoria con Boolean
     */
    RespuestaPorDefectoAuditoria<Boolean> crearReforzamiento(HttpServletRequest httpServletRequest,
                                                             MultipartFile[] multipartFiles,
                                                             MultipartFile[] constancias,
                                                             BodyEncriptado bodyEncriptado);


    /**
     * Devuelve RespuestaPorDefectoAuditoria<Boolean>.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ReforzamientoDTO
     * @return RespuestaPorDefectoAuditoria<Boolean>, Devuelve respuesta para auditoria con Boolean
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarReforzamiento(HttpServletRequest httpServletRequest,
                                                                  MultipartFile[] constancias,
                                                                  BodyEncriptado bodyEncriptado);


    /**
     * Devuelve RespuestaPorDefectoAuditoria<Boolean>.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ReforzamientoDTO
     * @return RespuestaPorDefectoAuditoria<Boolean>, Devuelve respuesta para auditoria con Boolean
     */
    RespuestaPorDefectoAuditoria<Boolean> removerReforzamiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PaginacionResponse<DocumentoDTO>
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado         objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptado);

}
