package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ActividadOcupacionalDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ActividadOcupacionalService {

    RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadOcupacionalDTO>>
    obtenerActividadesOcupacionalesPorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> crearActividadOcupacional(HttpServletRequest httpServletRequest,
                                                                                    BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> eliminarActividadOcupacional(HttpServletRequest httpServletRequest,
                                                                                       BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> obtenerActividadOcupacionalPorToken(HttpServletRequest httpServletRequest,
                                                                                              BodyEncriptado bodyEncriptado);

}
