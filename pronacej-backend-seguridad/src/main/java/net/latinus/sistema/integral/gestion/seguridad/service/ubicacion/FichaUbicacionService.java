package net.latinus.sistema.integral.gestion.seguridad.service.ubicacion;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.FichaUbicacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface FichaUbicacionService {

    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaUbicacionDTO>> obtenerListaPaginada(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<FichaUbicacionDTO> obtenerPorTokenIdentificador(
            HttpServletRequest httpServletRequest,
            String tokenIdentificador
    );

    RespuestaPorDefectoAuditoria<FichaUbicacionDTO> crearEditar(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<FichaUbicacionDTO> eliminar(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );
}

