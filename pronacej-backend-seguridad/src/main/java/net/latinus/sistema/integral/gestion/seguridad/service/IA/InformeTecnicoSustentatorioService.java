package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeTecnicoSustentatorioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface InformeTecnicoSustentatorioService {
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeTecnicoSustentatorioDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los informes técnicos.
     *
     * @return RespuestaPorDefectoAuditoria<InformeTecnicoSustentatorioDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeTecnicoSustentatorioDTO>> obtenerInformesTecnicosPaginado(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeTecnicoSustentatorioDTO si el informe técnico se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado objeto InformeTecnicoSustentatorioDTO a crear.
     *
     * @return RespuestaPorDefectoAuditoria<InformeTecnicoSustentatorioDTO>
     */
    RespuestaPorDefectoAuditoria<InformeTecnicoSustentatorioDTO> crearInformeTecnico(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado);
    
    /**
     * Elimina un informe técnico del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del informe técnico a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarInformeTecnico(
            HttpServletRequest httpServletRequest, 
            BodyEncriptado bodyEncriptado);

}