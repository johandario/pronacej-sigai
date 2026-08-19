package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.IngresoCentroJuvenilDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface IngresoCentroJuvenilService {
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con la paginacion de los ingresos a los centros
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene la información para la paginación de las fichas y el token id de la ficha medica
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<IngresoCentroJuvenilDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<IngresoCentroJuvenilDTO>> getCentrosJuvenilesByTokenIdFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el ingreso a centro creado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto IngresoCentroJuvenil a crear
     *
     * @return RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> devuelve el ingreso a centro creado
     */
    RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> postIngresoCentroJuvenil(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el ingreso a centro editado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el objeto IngresoCentroJuvenil a editar
     *
     * @return RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> devuelve el ingreso a centro editado
     */
    RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> updateIngresoCentroJuvenil(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con un Booleano que indica si el ingreso a centro ha sido eliminado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado contiene el token identificador del ingreso a centro a eliminar
     *
     * @return RespuestaPorDefectoAuditoria<Boolean> devuelve si el centro ha sido eliminado
     */
    RespuestaPorDefectoAuditoria<Boolean> deleteIngresoCentroJuvenil(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
