package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIngresoDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.FichaIngresoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface FichaIngresoDocumentoService {

    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest,
                                                              BodyEncriptado bodyEncriptado,
                                                              MultipartFile multipartFile);

    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     FichaIngresoDocumentoRequest fichaIngresoDocumentosRequest);

    public RespuestaPorDefectoAuditoria<FichaIngresoDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest,
                                                                                              FichaIngresoDocumentoDTO fichaIngresoDocumentoDTO);

    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentosFichaIngreso(HttpServletRequest httpServletRequest,
                                                                                     FichaIngresoDocumentoRequest fichaIngresoDocumentosRequest);
}
