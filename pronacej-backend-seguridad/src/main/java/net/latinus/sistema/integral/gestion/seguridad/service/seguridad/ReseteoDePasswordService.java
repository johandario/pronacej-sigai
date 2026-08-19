package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.ReseteoDePasswordDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ReseteoDeContraseniaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ReseteoDePasswordService {

    /**
     * Empieza un proceso de reseteo de contraseña de un usuario por el username
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param reseteoDeContraseniaRequest ReseteoDeContraseniaRequest
     *
     * @return RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO>
     */
    RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> empezarAccionDeReseteoDePassword(HttpServletRequest httpServletRequest,
                                                                                        ReseteoDeContraseniaRequest reseteoDeContraseniaRequest);

    /**
     * Verifica el proceso de reseteo de contraseña si esta activo para realizarse
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param reseteoDePasswordDTO ReseteoDePasswordDTO
     *
     * @return RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO>
     */
    RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> verificarReseteoDePassword(HttpServletRequest httpServletRequest,
                                                                                  ReseteoDePasswordDTO reseteoDePasswordDTO);

    /**
     * Reseta la contraseña de un usuario por el token identificador
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param reseteoDePasswordDTO ReseteoDePasswordDTO
     *
     * @return RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO>
     */
    RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> reseteoDePassword(HttpServletRequest httpServletRequest,
                                                                         ReseteoDePasswordDTO reseteoDePasswordDTO);

    /**
     * Cancela un proceso de reseto pendiente
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param reseteoDePasswordDTO ReseteoDePasswordDTO
     *
     * @return RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO>
     */
    RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> cancelarReseteo(HttpServletRequest httpServletRequest,
                                                          ReseteoDePasswordDTO reseteoDePasswordDTO);
}
