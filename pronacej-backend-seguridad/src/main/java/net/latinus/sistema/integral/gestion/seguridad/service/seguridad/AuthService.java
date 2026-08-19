package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CreacionDeUsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.LoginResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CreacionDeRol;

public interface AuthService {

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<LoginResponse>
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado objeto body encriptado.
     *
     * @return RespuestaPorDefectoAuditoria<LoginResponse>
     */
    RespuestaPorDefectoAuditoria<LoginResponse> loginUserSistema(HttpServletRequest httpServletRequest,
                                                                 BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<List<MenuDTO>>
     *
     * @param rol Rol.
     * @param empresa Empresa.
     * @parama esCompact Boolean declara si es o no compact
     *
     * @return RespuestaPorDefectoAuditoria<List < MenuDTO>>
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> crearMenuPorRolYEmpresa(Rol rol, Empresa empresa, Boolean esCompact);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<LoginResponse>
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<LoginResponse>
     */
    RespuestaPorDefectoAuditoria<LoginResponse> verificarJwt(HttpServletRequest httpServletRequest);

    /**
     * Crea un usuario del sistema y le asigna un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado CreacionDeUsuarioSistema datos del usuario a crear.
     *
     * @return RespuestaPorDefectoAuditoria<CreacionDeUsuarioSistema>
     */
    RespuestaPorDefectoAuditoria<CreacionDeUsuarioSistema> creaUnUsuarioDelSistema(HttpServletRequest httpServletRequest,
                                                                                   BodyEncriptado bodyEncriptado);
    
    /**
     * Obten una lista de usuarios del sistema con el rol paginado
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los usuarios.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < CreacionDeUsuarioSistema>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> obtenerUsuarioDelSistema(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);

    /**
     * Obten una lista de usuarios activos del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los usuarios.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < UsuarioSistema>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> obtenerUsuarioValidosDelSistema(HttpServletRequest httpServletRequest,
                                                                                                     BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un usuario del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del usuario a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarUsuario(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Bloquea la relacion entre el usuario la empresa y el rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del usuario a bloquear.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> bloquearUsuarioSistema(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
        
    /**
     * Crea un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado CreacionDeUsuarioSistema datos del rol a crear.
     *
     * @return RespuestaPorDefectoAuditoria< CreacionDeRol >
     */
    RespuestaPorDefectoAuditoria<CreacionDeRol> creaUnRol (HttpServletRequest httpServletRequest,
                                                           BodyEncriptado bodyEncriptado);

    /**
     * Obten una lista de roles
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los roles.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < CreacionDeRol>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> obtenerRoles(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);

    /**
     * Elimina un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del rol a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Bloquea la relacion del rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del rol a bloquear.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> bloquearRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Crea los permisos para las pantallas menus asociados a un rol y empresa
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado Request datos del rol y los menus con los que se va a relacionar para los permisos.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> crearRelacionMenusRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * carga los menus o pantallas a los que tiene acceso un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado Request datos del rol
     *
     * @return RespuestaPorDefectoAuditoria< List < MenuDTO > >
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusAccesiblesPorRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Crea un usuario del sistema y le asigna un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado CreacionDeUsuarioSistema datos del usuario a crear.
     *
     * @return RespuestaPorDefectoAuditoria<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<MenuDTO> creaUnMenuDelSistema(HttpServletRequest httpServletRequest,
                                                                                   BodyEncriptado bodyEncriptado);
    
       /**
     * Obten una lista de menus del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los usuarios.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<MenuDTO>> obtenerMenuDelSistema(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un menú del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del usuario a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarMenu(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
        
    /**
     * Verifica los permisos con una clave para la pantalla
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado String con la clave o nemonico de la pantalla o menu.
     *
     * @return RespuestaPorDefectoAuditoria<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<MenuDTO> verificarPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> obtenerRolesPorFiltro(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<LoginResponse>
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado objeto body encriptado.
     *
     * @return RespuestaPorDefectoAuditoria<LoginResponse>
     */
    RespuestaPorDefectoAuditoria<LoginResponse> cambioJerarquiaUserSistema(HttpServletRequest httpServletRequest,
                                                                 BodyEncriptado bodyEncriptado);
}
