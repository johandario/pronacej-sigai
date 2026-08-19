package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;

public interface JwtProviderService {

    /**
     * Verifica si el header del request coincide con el registrado en el backend
     *
     * @param httpServletRequest Objeto HttpServletRequest de tomcat.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    public RespuestaPorDefectoAuditoria<Boolean> verificarConsumoDirecto(HttpServletRequest httpServletRequest);

    /**
     * Devuelve un jwt con el subject enviado
     *
     * @param subject String que se va a insertar en el jwt.
     * @param idEmpresa Long id de la empresa.
     * @param parametroDelSistemaService ParametroDelSistemaService servicio parametro del sistema.
     *
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> crearJwt(String subject, Long idEmpresa,
                                                         ParametroDelSistemaService parametroDelSistemaService);


    /**
     * Devuelve un boolean para detectar si el jwt aún no esta expirado
     *
     * @param jwt String jwt.
     * @param tokenEmpresa Long id de la empresa.
     *
     * @return boolean
     */
    public boolean validDateToken(String jwt, String tokenEmpresa);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<BodyJwtValido> con las entidades de la db rol, usuario sistema y empresa
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<BodyJwtValido>
     */
    public RespuestaPorDefectoAuditoria<BodyJwtValido> obtenerBodyJwtApp(HttpServletRequest httpServletRequest);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<BodyJwtValido> con las entidades de la db rol, usuario sistema y empresa no valida la fecha
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<BodyJwtValido>
     */
    public RespuestaPorDefectoAuditoria<BodyJwtValido> obtenerBodyJwtAppNoValidarSesion(HttpServletRequest httpServletRequest);
}
