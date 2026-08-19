package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeFinalAsistenciaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface InformeFinalAsistenciaService {
    /**
     * Obtener todos los informes finales de asistencia
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAsistenciaDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos InformeFinalAsistenciaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAsistenciaDTO>> obtenerInformes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crear informe final de asistencia
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado bodyEncriptado dato que continene PlanTratamientoIndDTO a ser creado o editado.
     * @return RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO>, Devuelve respuesta para auditoria  objeto InformeFinalAsistenciaDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> crearInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminar informe final de asistencia
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado bodyEncriptado dato que continene PlanTratamientoIndDTO a ser eliminado.
     * @return RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO>, Devuelve respuesta para auditoria  objeto InformeFinalAsistenciaDTO eliminado
     */
    RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> eliminarInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
