package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndIntervDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlanTratamientoIndIntervService {

    /**
     * Obtiene un PlanTratamientoIndIntervDTO por su token identificador.
     *
     * @param bodyEncriptado el token identificador del PlanTratamientoIndInterv
     * @return el RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> correspondiente
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> getByTokenIdentificador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Actualiza un PlanTratamientoIndInterv basado en el DTO proporcionado.
     *
     * @param bodyEncriptado el PlanTratamientoIndIntervDT que se actualizará
     * @return el RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> correspondiente
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> updatePlanTratamientoIndInterv(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
