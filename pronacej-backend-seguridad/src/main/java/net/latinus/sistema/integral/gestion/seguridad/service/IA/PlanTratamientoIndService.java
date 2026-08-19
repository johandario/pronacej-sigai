package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlanTratamientoIndService {
    /**
     * Obtener todos los planes de tratamiento individual
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos PlanTratamientoIndDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndDTO>> obtenerPlanes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener un plan de tratamiento individual por identificador
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param id Id identificador único de objecto a consultar.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> obtenerPlanPorId(HttpServletRequest httpServletRequest, Long id);

    /**
     * Obtener plan de tratamiento individual por estado Activo
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param id Id identificador único de objecto a consultar.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> obtenerPlanActivo(HttpServletRequest httpServletRequest, String tokenIdentificadorFicha);


    /**
     * Crear un plan de tratamiento individual
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene PlanTratamientoIndDTO a ser creado o editado.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> crearPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminado lógico de un plan de tratamiento individual
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene PlanTratamientoIndDTO a ser elminado.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndDTO eliminado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> eliminarPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
