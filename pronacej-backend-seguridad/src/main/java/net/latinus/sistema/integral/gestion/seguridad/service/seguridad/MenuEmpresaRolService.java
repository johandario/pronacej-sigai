package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Menu;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.model.both.MenuEmpresaRolDTO;

public interface MenuEmpresaRolService {

    /**
     * Crea un registro en menuEmpresaRol y lo devuelve RespuestaPorDefectoAuditoria<MenuEmpresaRolDTO>
     *
     * @param empresa Empresa objeto empresa del sistema.
     * @param rol Rol objeto rol del sistema.
     * @param menu Menu objeto menu del sistema.
     * @param usuarioQueCrea usuario que crea la relacion
     * @param ip ip del usuario que crea la relacion
     *
     * @return RespuestaPorDefectoAuditoria< MenuEmpresaRolDTO >
     */
    RespuestaPorDefectoAuditoria<MenuEmpresaRolDTO> crearMenuEmpresaRol(Empresa empresa, Rol rol, Menu menu, UsuarioSistema usuarioQueCrea, String ip);

    /**
     * Elimina un registro en menuEmpresaRol y devuelve true o false
     *
     * @param empresa Empresa objeto empresa del sistema.
     * @param rol Rol objeto rol del sistema.
     * @param menu Menu objeto menu del sistema.
     * @param usuarioQueCrea usuario que elimina la relacion
     * @param ip ip del usuario que elimina la relacion
     *
     * @return RespuestaPorDefectoAuditoria< Boolean >
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarMenuEmpresaRol(Empresa empresa, Rol rol, Menu menu, UsuarioSistema usuarioQueCrea, String ip);

    /**
     * Devuelve una lista de menuEmpresaRol de acuerdo a un rol y empresa
     *
     * @param empresa Empresa objeto empresa del sistema.
     * @param rol Rol objeto rol del sistema.
     *
     * @return List<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<List<MenuEmpresaRolDTO>> obtenerTodosPorEmpresaYRol(Empresa empresa, Rol rol);
    
}
