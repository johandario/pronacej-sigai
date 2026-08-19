package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CargosJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

/**
 *
 * @author welli
 */
public interface CargosJerarquiaService {
    
        /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de cargos por jerarquia
     *
     * @param httpServletRequest request peticion.
     *
     * @return RespuestaPorDefectoAuditoria<List<CargosJerarquiaDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CargosJerarquiaDTO>> obtenerCargosJerarquias(HttpServletRequest httpServletRequest);

    
    /**
     * Obten una lista de cargos jerarquia
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos para obtener todos los cargos jerarquia.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < CargosJerarquiaDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> obtenerCargosJerarquiasPaginado(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado);
    
            
    /**
     * Crea un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado CargosJerarquiaDTO datos del cargo jerarquia a crear.
     *
     * @return RespuestaPorDefectoAuditoria< CargosJerarquiaDTO >
     */
    RespuestaPorDefectoAuditoria<CargosJerarquiaDTO> crearCargoJerarquia (HttpServletRequest httpServletRequest,
                                                           BodyEncriptado bodyEncriptado);
    
    
    /**
     * Elimina un rol
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos del cargo a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarCargoJerarquia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> obtenerCargosJerarquiaPorValor(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
