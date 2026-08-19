package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.DocumentoDTOFichaPrincipal;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaDeIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionTipoDeDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface FichaIdentificacionDocumentoService {

    /**
     * Sube los documentos al servidor de alfresco y devuelve un objeto RespuestaPorDefectoAuditoria con el objeto documento DTO
     *
     * @param httpServletRequest request peticion.
     * @param fichaPrincipalDocumentoDTO FichaPrincipalDocumentoDTO
     * @param multipartFile MultipartFile archivo que se va a subir
     *
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<DocumentoDTOFichaPrincipal> subirDocumento(HttpServletRequest httpServletRequest,
                                                                            FichaPrincipalDocumentoDTO fichaPrincipalDocumentoDTO,
                                                                            MultipartFile multipartFile);


    /**
     * Edita los documentos al servidor de alfresco y devuelve un objeto RespuestaPorDefectoAuditoria con el objeto documento DTO
     *
     * @param httpServletRequest request peticion.
     * @param fichaDeIdentificacionDocumentoDTO FichaDeIdentificacionDocumentoDTO
     * @param multipartFile MultipartFile archivo que se va a subir
     *
     * @return RespuestaPorDefectoAuditoria<FichaDeIdentificacionDocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<FichaDeIdentificacionDocumentoDTO> editarDocumento(HttpServletRequest httpServletRequest,
                                                                             FichaDeIdentificacionDocumentoDTO fichaDeIdentificacionDocumentoDTO,
                                                                            MultipartFile multipartFile);


    /**
     * Obten todos los documentos asociados a la ficha de identificación
     *
     * @param httpServletRequest request peticion.
     * @param fichaPrincipalDocumentosRequest FichaPrincipalDocumentosRequest.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < FichaDeIdentificacionDocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaDeIdentificacionDocumentoDTO>> obtenerDocumentosDeLaFichaDeIdentificacion(HttpServletRequest httpServletRequest,
                                                                                                                                   FichaPrincipalDocumentosRequest fichaPrincipalDocumentosRequest);


    /**
     * Remueve la relacion entre la ficha de identificacion y el documento
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     * @param fichaIdentificacionDocumentoDTO FichaIdentificacionDocumentoDTO
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionDocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionDocumentoDTO> eliminarRelacionConDocumento(
            HttpServletRequest httpServletRequest, FichaIdentificacionDocumentoDTO fichaIdentificacionDocumentoDTO
    );
}
