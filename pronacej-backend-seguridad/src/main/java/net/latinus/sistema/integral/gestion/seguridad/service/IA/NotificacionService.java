package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.NotificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface NotificacionService {

    /**
     * Obtiene las notificaciones enviadas asignadas al adolescente
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las fichas de ingreso.
     *
     * @return RespuestaPorDefectoAuditoria<NotificacionDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<NotificacionDTO>> obtenerNotificacionesPorToken(HttpServletRequest httpServletRequest,
                                                                                         BodyEncriptado bodyEncriptado);

    /**
     * Envia un email y guarda la notificacion en la base de datos (importante que el orden de los
     * archivos enviados coincidan con el body y con el multifilespart)
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las fichas de ingreso.
     * @param multipartFiles MultipartFile[] archivos fisicos recibidos
     *
     * @return RespuestaPorDefectoAuditoria<NotificacionEmailDTO>
     */
    RespuestaPorDefectoAuditoria<NotificacionDTO> enviarNotificacion(HttpServletRequest httpServletRequest,
                                                                     BodyEncriptado bodyEncriptado,
                                                                     MultipartFile[] multipartFiles);

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
