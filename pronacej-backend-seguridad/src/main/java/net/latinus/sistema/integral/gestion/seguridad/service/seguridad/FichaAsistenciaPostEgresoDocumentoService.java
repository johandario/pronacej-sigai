package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaAsistenciaPostEgresoDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PertenenciaDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaAsistenciaPostEgresoDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface FichaAsistenciaPostEgresoDocumentoService {
    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest,
                                                              BodyEncriptado bodyEncriptado,
                                                              MultipartFile multipartFile);

    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, FichaAsistenciaPostEgresoDocumentosRequest fichaAsistenciaPostEgresoDocumentosRequest);

    public RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, FichaAsistenciaPostEgresoDocumentoDTO fichaAsistenciaPostEgresoDocumentoDTO);

}
