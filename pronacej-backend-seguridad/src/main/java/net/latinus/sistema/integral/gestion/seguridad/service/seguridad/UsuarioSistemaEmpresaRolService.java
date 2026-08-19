package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosDeSeguridadDeUsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaEmpresaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface UsuarioSistemaEmpresaRolService {

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO>
     *
     * @param ip String ip del request.
     * @param userAccion UsuarioSistema usuario del sistema que realiza la operación.
     * @param usuarioSistemaEmpresaRolDTO UsuarioSistemaEmpresaRolDTO objeto a crear.
     *
     * @return RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO>
     */
    RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> crearOActualizar(UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO,
                                                                               String ip, UsuarioSistema userAccion);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> crea un usuario directo
     *
     * @param usuarioSistemaEmpresaRolDTO UsuarioSistemaEmpresaRolDTO datos de la relacion a crear.
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @returnRespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO>
     */
    RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> crearUsuarioDirecto(HttpServletRequest httpServletRequest, UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<DatosDeSeguridadDeUsuarioSistemaDTO> obteniendo los datos de seguridad de un usuario
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @returnRespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO>
     */
    RespuestaPorDefectoAuditoria<DatosDeSeguridadDeUsuarioSistemaDTO> obtenerDataDeSeguridad(
            HttpServletRequest httpServletRequest);

}
