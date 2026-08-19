package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.ConsultaAtencionIntegralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ConsultaAtencionIntegralService {

    RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> crearConsulta(HttpServletRequest request, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PaginacionResponse<ConsultaAtencionIntegralDTO>> getConsultaAtencionByIdFichaMedica (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> getConsultaActividadIntegralByIdTokenId (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<Boolean> deleteConsultaActividadIntegral(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
