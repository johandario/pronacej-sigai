package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.FichaMedicaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface FichaMedicaEnfermedadService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con todas las fichas medicas
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene la información para la paginación de las enfemerdades asociadas a la ficha Medica
     *
     * @return RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<FichaMeditaEnfermedadDTO>>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaEnfermedadDTO>> getFichaMedicaEnfermedades(HttpServletRequest httpServletRequest,
                                                                                                          BodyEncriptado bodyEncriptado);

}
