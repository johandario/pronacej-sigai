package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeFinalAbiertoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface InformeFinalAbiertoService {
    /**
     * Obtener todos los informes
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado     PaginacionRequest dato que continene información de paginador de petición.
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < InformeFinalAbiertoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos InformeFinalAbiertoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAbiertoDTO>> obtenerInformes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crear o editar informe
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado     bodyEncriptado dato que continene InformeFinalAbiertoDTO a ser creado o editado.
     * @return RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO>, Devuelve respuesta para auditoria  objeto InformeFinalAbiertoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO> crearInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminar informe
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado     bodyEncriptado dato que continene InformeFinalAbiertoDTO a ser eliminado.
     * @return RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO>, Devuelve respuesta para auditoria  objeto InformeFinalAbiertoDTO eliminado
     */
    RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO> eliminarInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Sube un documento y lo asocia al registro respectivo de evaluación domiciliaria
     *
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado     Cuerpo encriptado con los datos de relación
     * @param multipartFiles     Archivo a subir
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest, MultipartFile[] multipartFiles, BodyEncriptado bodyEncriptado);

    /**
     * Obtiene todos los documentos asociados al registro de evaluación domiciliaria
     *
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado     Request con parámetros de paginación y búsqueda
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < DocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
