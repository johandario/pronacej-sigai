package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EstudiosDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EstudiosEstadisticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface EstudiosService {
    RespuestaPorDefectoAuditoria<PaginacionResponse<EstudiosDTO>> obtenerListaEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<EstudiosDTO> crearEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<EstudiosDTO> obtenerEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<Boolean> eliminarEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<EstudiosDTO> consultarInstitucionPorRuc(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<Long> obtenerCantidadUsuariosEstudiando(
            HttpServletRequest httpServletRequest
    );

    RespuestaPorDefectoAuditoria<List<EstudiosEstadisticoDTO>> obtenerEstadisticasEstudios(
            HttpServletRequest httpServletRequest
    );

    RespuestaPorDefectoAuditoria<Double> obtenerPorcentajeConvenioPronacej(
            HttpServletRequest httpServletRequest
    );


}
