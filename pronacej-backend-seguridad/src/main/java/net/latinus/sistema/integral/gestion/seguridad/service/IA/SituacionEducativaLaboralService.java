
package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AreasSituacionEducativaLaboralOcioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionEducativaLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionEducativaLaboralOcioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface SituacionEducativaLaboralService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data AreasSituacionEducativaLaboralOcioDTO
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado PaginacionRequest datos para obtener todas las situaciones areas educativas/laborales/ocio.
     *
     * @return RespuestaPorDefectoAuditoria<AreasSituacionEducativaLaboralOcioDTO>
     */
    public RespuestaPorDefectoAuditoria<AreasSituacionEducativaLaboralOcioDTO> obtenerAreasSituacionEducativaLaboralOcio(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SituacionEducativaLaboralOcioDTO Paginada
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado PaginacionRequest datos para obtener todas las situaciones educativas/laborales/ocio.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionEducativaLaboralOcioDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionEducativaLaboralOcioDTO>> obtenerSituacionesEducativasLaboralesOcio(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data LaboralDTO Paginada
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los laborales.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<LaboralDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<LaboralDTO>> obtenerLaborales(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data SituacionEducativaLaboralDTO si la situación educativa laboral se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto SituacionEducativaLaboralDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<SituacionEducativaLaboralDTO>
     */
    RespuestaPorDefectoAuditoria<SituacionEducativaLaboralDTO> crearSituacionEducativaLaboral(HttpServletRequest httpServletRequest,
                                                               BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina todos los elementos asociados a los DTO de SituacionEducativaLaboralOcioDTO
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del SituacionEducativaLaboralOcioDTO a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSituacionEducativaLaboralOcio(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina todos los elementos asociados a los DTO de LaboralDTO
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del LaboralDTO a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarLaboral(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    
}
