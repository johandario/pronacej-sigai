package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DetalleFichaAsistenciaPostEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface DetalleFichaAsistenciaPostEgresoService {

    /**
     * Obtiene los detalles de una ficha de asistencia post egreso según su token.
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO>>
    obtenerDetallesPorFichaAsistencia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crea o edita un detalle de ficha de asistencia post egreso.
     */
    RespuestaPorDefectoAuditoria<DetalleFichaAsistenciaPostEgresoDTO>
    crearOEditarDetalleFichaAsistencia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina un detalle de ficha de asistencia post egreso (cambia el estado a removido = true).
     */
    RespuestaPorDefectoAuditoria<Boolean>
    eliminarDetalleFichaAsistencia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
