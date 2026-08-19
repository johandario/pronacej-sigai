package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndIntervSeguiDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlanTratamientoIndIntervSeguiService {
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndIntervSeguiDTO>> obtenerSeguimientos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> crearSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
