package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndSeguiDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlanTratamientoIndSeguiService {    /**
     * Obtener todos los seguimientos de planes de tratamiento individual
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos PlanTratamientoIndSeguiDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiDTO>> obtenerSeguimientos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener un seguimiento de plan de tratamiento individual por identificador
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param id Id identificador único de objecto a consultar.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndSeguiDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> obtenerSeguimientoPorId(HttpServletRequest httpServletRequest, Long id);


    /**
     * Crear un seguimiento de plan de tratamiento individual
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene PlanTratamientoIndDTO a ser creado o editado.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndSeguiDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> crearSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminado lógico de seguimiento de plan de tratamiento individual
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene PlanTratamientoIndDTO a ser elminado.
     *
     * @return RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO>, Devuelve respuesta para auditoria  objeto PlanTratamientoIndSeguiDTO eliminado
     */
    RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
