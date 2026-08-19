package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.HistoricoEntradaSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface HistoricoEntradaSalidaService {

    RespuestaPorDefectoAuditoria<HistoricoEntradaSalidaDTO> obtenerHistoricoEntradaSalida(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptad);

}
