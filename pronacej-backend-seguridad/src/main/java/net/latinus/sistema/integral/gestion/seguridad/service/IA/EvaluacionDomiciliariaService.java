package net.latinus.sistema.integral.gestion.seguridad.service.IA;
import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionDomiciliariaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionDomiciliariaDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.EvaluacionDomiciliariaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface EvaluacionDomiciliariaService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionDomiciliariaDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos las evaluaciones domiciliarias.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionDomiciliariaDTO>> obtenerEvaluacionesDomiciliarias(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionDomiciliariaDTO si la evaluación domiciliaria se creo con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto EvaluacionDomiciliariaDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO> crearEvaluacionDomiciliaria(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina una evaluación domiciliaria del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de la evaluación domiciliaria a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionDomiciliaria(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data FichaIdentificacionDTO si la empresa se removio con exito
     *
     * @param httpServletRequest request peticion.
     * @param EvaluacionDomiciliariaDTO objeto evaluación domiciliaria dto.
     *
     * @return RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO> removerFicha(HttpServletRequest httpServletRequest,
                                                                EvaluacionDomiciliariaDTO evaluacionDomiciliariaDTO);
    
    /**
     * Sube un documento y lo asocia al registro respectivo de evaluación domiciliaria
     * 
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado Cuerpo encriptado con los datos de relación
     * @param multipartFile Archivo a subir
     * 
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest, 
                                                             BodyEncriptado bodyEncriptado, 
                                                             MultipartFile multipartFile);
    
    /**
     * Obtiene todos los documentos asociados al registro de evaluación domiciliaria
     * 
     * @param httpServletRequest Solicitud HTTP
     * @param evaluacionDomiciliariaDocumentosRequest Request con parámetros de paginación y búsqueda
     * 
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, 
                                                                                    EvaluacionDomiciliariaDocumentosRequest evaluacionDomiciliariaDocumentosRequest);
    
    /**
     * Elimina la relación entre un documento y una evaluación domiciliaria
     * 
     * @param httpServletRequest Solicitud HTTP
     * @param evaluacionDomiciliariaDocumentoDTO DTO con la información de la relación a eliminar
     * 
     * @return RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, 
                                                                                                 EvaluacionDomiciliariaDocumentoDTO evaluacionDomiciliariaDocumentoDTO);
}