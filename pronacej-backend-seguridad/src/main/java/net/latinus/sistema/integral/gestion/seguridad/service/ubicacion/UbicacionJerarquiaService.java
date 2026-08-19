package net.latinus.sistema.integral.gestion.seguridad.service.ubicacion;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.UbicacionJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

/**
 * Interface de servicio para UbicacionJerarquia
 * Define los contratos para las operaciones de ubicaciones jerárquicas
 */
public interface UbicacionJerarquiaService {

    /**
     * Obtiene una lista paginada de ubicaciones jerárquicas
     *
     * @param httpServletRequest request de la petición
     * @param bodyEncriptado objeto encriptado con parámetros de paginación
     * @return RespuestaPorDefectoAuditoria con PaginacionResponse de UbicacionJerarquiaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<UbicacionJerarquiaDTO>> obtenerListaPaginada(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtiene la lista completa de ubicaciones jerárquicas no removidas
     *
     * @param httpServletRequest request de la petición
     * @return RespuestaPorDefectoAuditoria con lista de UbicacionJerarquiaDTO
     */
    RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> obtenerListaCompleta(
            HttpServletRequest httpServletRequest);

    /**
     * Obtiene una ubicación jerárquica por su token identificador
     *
     * @param httpServletRequest request de la petición
     * @param tokenIdentificador token de identificación
     * @return RespuestaPorDefectoAuditoria con UbicacionJerarquiaDTO
     */
    RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> obtenerPorTokenIdentificador(
            HttpServletRequest httpServletRequest, String tokenIdentificador);

    /**
     * Obtiene los hijos de una ubicación jerárquica padre
     *
     * @param httpServletRequest request de la petición
     * @param tokenIdentificadorPadre token del padre
     * @return RespuestaPorDefectoAuditoria con lista de UbicacionJerarquiaDTO
     */
    RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> obtenerHijosPorTokenIdentificadorPadre(
            HttpServletRequest httpServletRequest, String tokenIdentificadorPadre);

    /**
     * Obtiene ubicaciones jerárquicas por token identificador de jerarquía centro
     *
     * @param httpServletRequest request de la petición
     * @param tokenIdentificadorCentro token de la jerarquía centro
     * @return RespuestaPorDefectoAuditoria con lista de UbicacionJerarquiaDTO
     */
    RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> obtenerPorTokenIdentificadorJerarquiaCentro(
            HttpServletRequest httpServletRequest, String tokenIdentificadorCentro);

    /**
     * Crea o edita una ubicación jerárquica
     *
     * @param httpServletRequest request de la petición
     * @param bodyEncriptado objeto encriptado con datos de UbicacionJerarquiaDTO
     * @return RespuestaPorDefectoAuditoria con UbicacionJerarquiaDTO creada o editada
     */
    RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> crearEditar(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina una ubicación jerárquica
     *
     * @param httpServletRequest request de la petición
     * @param bodyEncriptado objeto encriptado con token de identificación
     * @return RespuestaPorDefectoAuditoria con UbicacionJerarquiaDTO eliminada
     */
    RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> eliminar(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}


