package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DelitoEstadisticaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ExpedienteMatrizDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ExpedienteMatrizDetalleDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface ExpedienteMatrizService {

    RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> obtenerExpedientes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> obtenerExpedientePorNum(HttpServletRequest httpServletRequest, String numExpediente);

    RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> obtenerExpedientePorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> crearExpediente(HttpServletRequest httpServletRequest, ExpedienteMatrizDTO expedienteMatrizDTO);

    RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> eliminarExpediente(HttpServletRequest httpServletRequest, ExpedienteMatrizDTO expedienteMatrizDTO);

    RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> obtenerExpedientePorTokenFicha(HttpServletRequest httpServletRequest,  BodyEncriptado bodyEncriptado, String tokenIdentificador);

    RespuestaPorDefectoAuditoria<List<DelitoEstadisticaDTO>> obtenerEstadisticasDelitos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> obtenerExpedienteDetallePorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> obtenerExpedienteCabeceraYDetalleActualPorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
