package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface ActaExternamientoService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data ActaExternamientoDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado request peticion.
     * @return RespuestaPorDefectoAuditoria<ActaExternamientoDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<ActaExternamientoDTO>> obtenerActasExternamiento(HttpServletRequest httpServletRequest,
                                                                                                           BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto ActaExternamientoDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> crearActaExternamiento(HttpServletRequest httpServletRequest,
                                                                      BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoPsicologicoDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarActaExternamiento(HttpServletRequest httpServletRequest,
                                                                           BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si el documento se sube con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado         objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirActaFirmada(HttpServletRequest httpServletRequest,
                                                              MultipartFile multipartFile,
                                                              BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto SeguimientoPsicologicoDTO.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarActaExternamiento(HttpServletRequest httpServletRequest,
                                                                         BodyEncriptado bodyEncriptado);
}
