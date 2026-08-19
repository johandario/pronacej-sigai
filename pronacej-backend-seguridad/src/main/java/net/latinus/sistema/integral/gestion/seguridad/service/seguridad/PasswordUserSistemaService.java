package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PasswordUserSistemaService {

    /**
     * Cambia la contraseña del usuario logeado en el sistema y actualiza datos de seguridad
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado objeto CambioDePasswordRequest
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarDatosDeSeguridad(HttpServletRequest httpServletRequest,
                                                           BodyEncriptado bodyEncriptado);
}
