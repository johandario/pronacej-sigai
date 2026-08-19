package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.DatosFamiliaresDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface DatosFamiliaresDocumentoService {

    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest,
                                                             BodyEncriptado bodyEncriptado,
                                                             MultipartFile multipartFile);

    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, 
                                                                                     DatosFamiliaresDocumentosRequest datosFamiliaresDocumentosRequest);

    RespuestaPorDefectoAuditoria<DatosFamiliaresDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, 
                                                                                           DatosFamiliaresDocumentoDTO datosFamiliaresDocumentoDTO);

    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumentoFichaPsicoSocial(HttpServletRequest httpServletRequest,
                                                              BodyEncriptado bodyEncriptado,
                                                              MultipartFile multipartFile);

    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentosFichaPsicoSocial(HttpServletRequest httpServletRequest,
                                                                                     DatosFamiliaresDocumentosRequest datosFamiliaresDocumentosRequest);
}