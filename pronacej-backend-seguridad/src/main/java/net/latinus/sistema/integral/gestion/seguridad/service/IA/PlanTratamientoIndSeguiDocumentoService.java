package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndSeguiDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PlanTratamientoIndSeguiDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface PlanTratamientoIndSeguiDocumentoService {
    RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest,
                                                              BodyEncriptado bodyEncriptado,
                                                              MultipartFile multipartFile);

    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, PlanTratamientoIndSeguiDocumentoRequest planTratamientoIndSeguiDocumentoRequest);

    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, PlanTratamientoIndSeguiDocumentoDTO planTratamientoIndSeguiDocumentoDTO);

}
