package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PersonaRelacionadaEnfermedadService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con todas las fichas medicas
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene la información para la paginación de las enfemerdades asociadas a la persona relacionada
     *
     * @return RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<PersonaRelacionadaEnfermedadDTO>>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaEnfermedadDTO>> getPersonaRelacionadaEnfermedades(HttpServletRequest httpServletRequest,
                                                                                                                        BodyEncriptado bodyEncriptado);
}
