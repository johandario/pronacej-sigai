package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Menu;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.NavigationFuseResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface MenuService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<MenuDTO>>>
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param menuDTOList List<MenuDTO> lista de menus a crear.
     *
     * @return RespuestaPorDefectoAuditoria<List < RespuestaPorDefectoAuditoria < MenuDTO>>>
     */
    RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<MenuDTO>>> crearOEditarMenus(HttpServletRequest httpServletRequest,
                                                                                                List<MenuDTO> menuDTOList);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<MenuDTO>
     *
     * @param ip string ip del request.
     * @param usuarioSistema UsuarioSistema objeto usuario del sistema.
     * @param menuDTO MenuDTO menu dtor.
     *
     * @return RespuestaPorDefectoAuditoria<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<MenuDTO> crearOEditarMenu(String ip, UsuarioSistema usuarioSistema,
                                                           MenuDTO menuDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<List<MenuDTO>> con el jwt del header del request
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<NavigationFuse>
     */
    RespuestaPorDefectoAuditoria<NavigationFuseResponse> crearMenuPorJwtApp(HttpServletRequest httpServletRequest);

    /**
     * Devuelve una lista de menu dto
     *
     * @param menuPadres List<Menu> menus de la db que son padres principales.
     * @param esCompact es una lista de menu compact
     * @param mostrarEnElFront Boolean mostrar en el front
     * @param rol empresa
     * @param empresa rol
     *
     * @return List<MenuDTO>
     */
    List<MenuDTO> obtenerMenusDeMenusPadres(List<Menu> menuPadres, Boolean esCompact, Boolean mostrarEnElFront, Rol rol, Empresa empresa);


    /**
     * Devuelve un RespuestaPorDefectoAuditoria<List<MenuDTO>>
     *
     * @param rol Rol.
     * @param empresa Empresa.
     * @param esCompact es una lista de menu compact
     *
     * @return RespuestaPorDefectoAuditoria<List < MenuDTO>>
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> crearMenuPorRolYEmpresa(Rol rol, Empresa empresa, Boolean esCompact);


    /**
     * Devuelve una lista de menu dto
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return List<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerTodosLosMenu(HttpServletRequest httpServletRequest);

    /**
     * Devuelve una lista de menu dto habilitados para permisos por rol/usuario-rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return List<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusPermisos(HttpServletRequest httpServletRequest);

    /**
     * Devuelve una lista de menu dto
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return List<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusPorEmpresa(HttpServletRequest httpServletRequest);


    /**
     * Devuelve el menu DTO editado
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param menuDTO menuDTO
     *
     * @return RespuestaPorDefectoAuditoria<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<MenuDTO> editarTituloYRealizaAuditoria(HttpServletRequest httpServletRequest,
                                                                              MenuDTO menuDTO);
    
    /**
     * Devuelve una lista de menu dto
     *
     * @param idEmpresa empresa por la que se va filtrar la obtencion de menus
     * @param rol rol que tenga permiso para los menus a obtener
     *
     * @return List<MenuDTO>
     */    
    RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusAccesiblesPorRol(Long idEmpresa, Rol rol);

    /**
     * Devuelve una lista de menu dto
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return List<MenuDTO>
     */
    RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusPadres(HttpServletRequest httpServletRequest);
}
