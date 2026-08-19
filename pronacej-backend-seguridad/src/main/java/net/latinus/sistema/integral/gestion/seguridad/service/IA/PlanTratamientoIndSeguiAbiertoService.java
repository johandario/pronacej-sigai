package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndSeguiAbiertoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlanTratamientoIndSeguiAbiertoService {
    /**
     * Obtiene una lista de PlanTratamientoIndSeguiAbiertoDTO
     *
     * @param bodyEncriptado el token identificador del PlanTratamientoIndSeguiAbierto
     * @return el RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>> correspondiente
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>> obtenerFichasSeguimientoAbierto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crea/Actualiza un PlanTratamientoIndSeguiAbiertoDTO basado en el DTO proporcionado.
     *
     * @param bodyEncriptado el PlanTratamientoIndIntervDT que se actualizará
     * @return el RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> correspondiente
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> crearEditarFichaSeguimientoAbierto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminado lógico de un PlanTratamientoIndSeguiAbiertoDTO basado en el DTO proporcionado.
     *
     * @param bodyEncriptado el PlanTratamientoIndIntervDT que se actualizará
     * @return el RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> correspondiente
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> eliminarFichaSeguimientoAbierto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
