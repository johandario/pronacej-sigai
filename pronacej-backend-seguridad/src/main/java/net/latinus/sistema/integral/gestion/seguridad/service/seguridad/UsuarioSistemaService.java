package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.CargaMasivaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.CargaMasivaResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.UserDataResponse;

public interface UsuarioSistemaService {
    
    /**
     * Endpoint para realizar la carga masiva de usuarios
     * 
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param cargaMasivaRequest Objeto con la información de los usuarios a cargar
     * @return Respuesta con el resultado de la operación
     */
    RespuestaPorDefectoAuditoria<CargaMasivaResponse> subirUsuariosCargaMasiva(HttpServletRequest httpServletRequest, CargaMasivaRequest cargaMasivaRequest);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<UsuarioSistemaDTO>
     *
     * @param usuarioSistemaDTO UsuarioSistemaDTO datos del usuario a crear.
     * @param usuarioQueCrea UsuarioSistema usuario que crea el usuario.
     * @param ipQueCrea Stirng ip que realiza la operación.
     *
     * @return RespuestaPorDefectoAuditoria<UsuarioSistemaDTO>
     */
    RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> crearUsuario(UsuarioSistemaDTO usuarioSistemaDTO, UsuarioSistema usuarioQueCrea,
                                                                 String ipQueCrea);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> crea un usuario directo
     *
     * @param usuarioSistemaDTO UsuarioSistemaDTO datos del usuario a crear.
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<UsuarioSistemaDTO>
     */
    RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> crearUsuarioDirecto(HttpServletRequest httpServletRequest, UsuarioSistemaDTO usuarioSistemaDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<UserDataResponse> con la data del usuario para mostrar en el front
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<UserDataResponse>
     */
    RespuestaPorDefectoAuditoria<UserDataResponse> obtenerDataDelUsuarioLogeado(HttpServletRequest httpServletRequest);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<UserDataResponse> con la data del usuario para mostrar en el front
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado datos del usuario a cambiar
     *
     * @return RespuestaPorDefectoAuditoria<UserDataResponse>
     */
    RespuestaPorDefectoAuditoria<UserDataResponse> actualizarDatosDePerfilDelUsuario(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
