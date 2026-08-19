package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SeguimientoSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface SeguimientoSocialService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SeguimientoSocialDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     PaginacionRequest datos para obtener todos los seguimientos sociales.
     * @return RespuestaPorDefectoAuditoria<SeguimientoSocialDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoSocialDTO>> obtenerSeguimientosSociales(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);

    /**
     * Elimina un seguimiento social del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado     PaginacionRequest datos del seguimiento social a eliminar.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimientoSocial(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SeguimientoSocialDTO si el seguimiento social se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado     objeto SeguimientoSocialDTO a crear.
     * @return RespuestaPorDefectoAuditoria<SeguimientoSocialDTO>
     */
    RespuestaPorDefectoAuditoria<SeguimientoSocialDTO> crearSeguimientoSocial(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);

    /**
     * Sube un documento y lo asocia al registro respectivo de evaluación domiciliaria
     *
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado     Cuerpo encriptado con los datos de relación
     * @param multipartFiles     Archivo a subir
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                          BodyEncriptado bodyEncriptado,
                                                          MultipartFile[] multipartFiles);

    /**
     * Obtiene todos los documentos asociados al registro de evaluación domiciliaria
     *
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado     Request con parámetros de paginación y búsqueda
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse <DocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptado);

}