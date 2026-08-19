package net.latinus.sistema.integral.gestion.seguridad.service.documentos;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentoService {

    /**
     * Guarda un documento en el servicio de file system del cliente (Alfresco)
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param idNodoAlfresco String id del nodo de alfresco
     * @param multipartFile MultipartFile objeto que representa el archivo.
     * @param documentoDTO DocumentoDTO
     *
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumentoAlfresco(HttpServletRequest httpServletRequest,
                                                                      String idNodoAlfresco,
                                                                      MultipartFile multipartFile, DocumentoDTO documentoDTO);

    /**
     * Obten el documento fisico guardado en el fileSystem del Alfresco
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param tokenIdentificadorDocumento String token identificador de un documento
     *
     * @return RespuestaPorDefectoAuditoria<Resource>
     */
    RespuestaPorDefectoAuditoria<Resource> obtenerDocumentoFisico(HttpServletRequest httpServletRequest,
                                                                  String tokenIdentificadorDocumento);

    /**
     * Elimina el documento de la base de datos y renombralo en alfresco
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param tokenIdentificador String id del nodo de alfresco
     *
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<DocumentoDTO> eliminarDocumento(HttpServletRequest httpServletRequest,
                                                                      String tokenIdentificador);

    /**
     * Actualiza un documento en el servicio de file system del cliente (Alfresco)
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param multipartFile MultipartFile objeto que representa el archivo.
     * @param documentoDTO DocumentoDTO
     *
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<DocumentoDTO> actualizardocumento(HttpServletRequest httpServletRequest,
                                                                      MultipartFile multipartFile, DocumentoDTO documentoDTO);
}
