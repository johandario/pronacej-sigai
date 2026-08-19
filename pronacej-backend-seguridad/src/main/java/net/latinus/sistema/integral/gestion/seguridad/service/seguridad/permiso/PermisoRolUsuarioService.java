package net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioNombresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface PermisoRolUsuarioService {

    /**
     * Obten una lista de permisos de menús por rol
     *
     * @param httpServletRequest    datos del request.
     * @param bodyEncriptado body encriptado que contiene objeto de paginador
     * @return RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> contiene la lista de permisos de menu por usuario/rol
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PermisoRolUsuarioNombresDTO>> obtenerPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obten una lista de permisos de menús por rol
     *
     * @param httpServletRequest    datos del request.
     * @param tokenIdentificador token identificador del permiso
     * @return RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> contiene la lista de permisos de menu por usuario/rol
     */
    RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> obtenerPermisosPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);

    /**
     * Crear una lista de permisos por rol o usuario/funcionarioJerarquiaRol
     *
     * @param httpServletRequest datos del request.
     * @param bodyEncriptado body encriptadc con los permisos a crear o editar con menus, roles y usuario/funcionarioJerarquiaRol
     * @return RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> contiene la lista de permisos de menu por usuario/rol
     */
    RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> crearEditarPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminar una lista de permisos por rol o usuario/funcionarioJerarquiaRol
     *
     * @param httpServletRequest datos del request.
     * @param bodyEncriptado body encriptadc con los permisos a crear o editar con menus, roles y usuario/funcionarioJerarquiaRol
     * @return RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> contiene la lista de permisos de menu por usuario/rol
     */
    RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> eliminarPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obten los permisos por usuario y rol, en caso de que no tenga se obtiene el del rol (por defecto)
     *
     * @param httpServletRequest datos del request.
     * @return RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> contiene la lista de permisos de menu por usuario/rol
     */
    RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> obtenerPermisosUsuarioPorTokenFicha(HttpServletRequest httpServletRequest, String tokenFichaIdentificacion);

    /**
     * Validar permisos de una lista respecto a la ficha de identificación y datos provenientes del token JWT
     *
     * @param <T>                      tipo de objeto a procesar
     * @param lista                    cualquier objeto que herede CamposDTO
     * @param tokenFichaIdentificacion token de ficha de identificación a procesar
     * @param bodyJwtValido            objeto con información de token JWT
     */
    <T extends CamposDTO> void validarPermisoLista(List<T> lista, String tokenFichaIdentificacion, BodyJwtValido bodyJwtValido);

    /**
     * Validar permiso de realizar acción por objeto
     *
     * @param objeto                    objeto que hereda de CamposDTO
     * @param tokenFichaIdentificacion  token de ficha de identificación a procesar
     * @param bodyJwtValido             objeto con información de token JWT
     * @param nemonicoAccion            nemonico de la acción a validar
     * @return                          true/false dependiendo si tiene permiso para realizar la acción
     * @param <T>                       tipo de objeto a procesar
     */
    <T extends CamposDTO> boolean validarPermisoObjetoYAccion(T objeto, String tokenFichaIdentificacion, BodyJwtValido bodyJwtValido, String nemonicoAccion);
}
