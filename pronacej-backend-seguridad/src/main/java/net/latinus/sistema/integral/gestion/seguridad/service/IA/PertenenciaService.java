package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PertenenciaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PertenenciaService {
    RespuestaPorDefectoAuditoria<PaginacionResponse<PertenenciaDTO>> obtenerPertenenciasEncrypt(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PertenenciaDTO> obtenerPertenenciasPorId(HttpServletRequest httpServletRequest, Long id);

    RespuestaPorDefectoAuditoria<PertenenciaDTO> crearPertenencia(HttpServletRequest httpServletRequest, PertenenciaDTO pertenenciaDTO);

    RespuestaPorDefectoAuditoria<PertenenciaDTO> eliminarPertenencia(HttpServletRequest httpServletRequest, PertenenciaDTO pertenenciaDTO);

    /*List<PertenenciaDTO> obtenerPertenencias();
    PertenenciaDTO crearPertenencia(PertenenciaDTO pertenenciaDTO);*/

}
