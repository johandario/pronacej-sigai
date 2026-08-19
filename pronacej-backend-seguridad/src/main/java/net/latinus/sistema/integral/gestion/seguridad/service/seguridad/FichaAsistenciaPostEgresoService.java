package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaAsistenciaPostEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface FichaAsistenciaPostEgresoService {

    RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDTO> crearFichaAsistenciaPostEgreso(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    );

    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaAsistenciaPostEgresoDTO>> obtenerFichasAsistenciaPostEgreso
            (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<Boolean> eliminarFichaAsistenciaPostEgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
