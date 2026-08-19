package net.latinus.sistema.integral.gestion.seguridad.service.tras;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface TrasladoService {
    /**
     * Obtener todos los traslados
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos TrasladoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> obtenerTraslados(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener todos los traslados por ficha identificacion
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     * @param idFichaIdentificacion PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos TrasladoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> obtenerTrasladosPorIdFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, Long idFichaIdentificacion);

    /**
     * Obtener un traslado por UUID identificador
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param tokenIdentificador String identificador único de objecto a consultar.
     *
     * @return RespuestaPorDefectoAuditoria<TrasladoDTO>, Devuelve respuesta para auditoria  objeto TrasladoDTO buscado
     */
    RespuestaPorDefectoAuditoria<TrasladoDTO> obtenerTrasladoPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);

    /**
     * Crear o editar un traslado
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     *
     * @return RespuestaPorDefectoAuditoria<TrasladoDTO>, Devuelve respuesta para auditoria  objeto TrasladoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<TrasladoDTO> crearTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crear o editar un borrador de traslado
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     *
     * @return RespuestaPorDefectoAuditoria<TrasladoDTO>, Devuelve respuesta para auditoria  objeto TrasladoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<TrasladoDTO> guardarBorrador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Rechazar traslado
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     *
     * @return RespuestaPorDefectoAuditoria<TrasladoDTO>, Devuelve respuesta para auditoria  objeto TrasladoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<TrasladoDTO> rechazarTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Eliminado lógico de un traslado
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     *
     * @return RespuestaPorDefectoAuditoria<TrasladoDTO>, Devuelve respuesta para auditoria  objeto TrasladoDTO eliminado
     */
    RespuestaPorDefectoAuditoria<TrasladoDTO> eliminarTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Obtener listado de traslados por ID de Ficha Identificación.
     *
     * @param httpServletRequest HttpServletRequest para la validación del JWT.
     * @param idFichaIdentificacion ID del adolescente (ficha de identificación).
     * @return Respuesta con la lista de traslados relacionados al ID.
     */
    RespuestaPorDefectoAuditoria<List<TrasladoDTO>> obtenerListadoTrasladosPorAdolescente(HttpServletRequest httpServletRequest, Long idFichaIdentificacion);

    /**
     * Obtener todos los traslados por ficha identificacion
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos TrasladoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> obtenerTrasladosPorFichaIdentificacionTokenIdentificador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
