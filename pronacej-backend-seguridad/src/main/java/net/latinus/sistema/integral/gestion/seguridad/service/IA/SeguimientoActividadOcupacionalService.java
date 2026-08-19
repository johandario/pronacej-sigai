package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SeguimientoActividadOcupacionalDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface SeguimientoActividadOcupacionalService {

    RespuestaPorDefectoAuditoria<SeguimientoActividadOcupacionalDTO> crearSeguimiento(HttpServletRequest request, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoActividadOcupacionalDTO>> obtenerSeguimientosPorActividad(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<SeguimientoActividadOcupacionalDTO> obtenerSeguimientoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
