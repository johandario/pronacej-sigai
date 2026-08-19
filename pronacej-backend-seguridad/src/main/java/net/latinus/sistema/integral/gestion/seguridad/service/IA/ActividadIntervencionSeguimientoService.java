package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ActividadIntervencionSeguimientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ActividadIntervencionSeguimientoService {

    RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionSeguimientoDTO>>
    obtenerSeguimientosPorActividadId(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ActividadIntervencionSeguimientoDTO>
    crearActualizarActividadIntervencionSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


}
