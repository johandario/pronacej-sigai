package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.TrabajoLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.TrabajoLaboralEstadisticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;


public interface TrabajoLaboralService {

    RespuestaPorDefectoAuditoria<PaginacionResponse<TrabajoLaboralDTO>> obtenerListaTrabajoLaboral(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado
    );


    RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> crearTrabajoLaboral(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> obtenerTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<Boolean> eliminarTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<Long> obtenerCantidadTrabajoActivo(HttpServletRequest httpServletRequest);

    RespuestaPorDefectoAuditoria<List<TrabajoLaboralEstadisticoDTO>> obtenerEstadisticasTrabajoLaboral(
            HttpServletRequest httpServletRequest
    );

}