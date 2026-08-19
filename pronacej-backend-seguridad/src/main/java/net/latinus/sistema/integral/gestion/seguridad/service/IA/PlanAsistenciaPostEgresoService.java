package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanAsistenciaPostEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlanAsistenciaPostEgresoService {
    /**
     * Obtener todos los planes de asistencia
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<PlanAsistenciaPostEgresoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos PlanAsistenciaPostEgresoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlanAsistenciaPostEgresoDTO>> obtenerPlanes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crear plan de asistencia
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado bodyEncriptado dato que continene PlanTratamientoIndDTO a ser creado o editado.
     * @return RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO>, Devuelve respuesta para auditoria  objeto PlanAsistenciaPostEgresoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> crearPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener plan de asistencia por token identificador
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param tokenIdentificadorPlan token identificador de plan de asistencia.
     * @return RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO>, Devuelve respuesta para auditoria  objeto PlanAsistenciaPostEgresoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> obtenerPlanPorTokenIdentificador(HttpServletRequest httpServletRequest, String tokenIdentificadorPlan);

    /**
     * Eliminar plan de asistencia
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado bodyEncriptado dato que continene PlanTratamientoIndDTO a ser eliminado.
     * @return RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO>, Devuelve respuesta para auditoria  objeto PlanAsistenciaPostEgresoDTO eliminado
     */
    RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> eliminarPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
