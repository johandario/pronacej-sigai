package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;


import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface RolService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<RolDTO>
     *
     * @param rolDTO RolDTO datos del rol a crear.
     * @param usuarioQueCrea UsuarioSistema usuario que crea el rol.
     * @param ipQueCrea Stirng ip que realiza la operación.
     *
     * @return RespuestaPorDefectoAuditoria<RolDTO>
     */
    RespuestaPorDefectoAuditoria<RolDTO> crearOEditarRol(RolDTO rolDTO, UsuarioSistema usuarioQueCrea,
                                                      String ipQueCrea);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<RolDTO> crea un usuario directo
     *
     * @param rolDTO RolDTO datos del rol a crear.
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @returnRespuestaPorDefectoAuditoria<RolDTO>
     */
    RespuestaPorDefectoAuditoria<RolDTO> crearRolDirecto(HttpServletRequest httpServletRequest, RolDTO rolDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<List<RolDTO>> obtiene una lista de roles de una empresa por el jwt
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @returnRespuestaPorDefectoAuditoria<RolDTO>
     */
    RespuestaPorDefectoAuditoria<List<RolDTO>> obtenerRolesDeEmpresa(HttpServletRequest httpServletRequest);
}
