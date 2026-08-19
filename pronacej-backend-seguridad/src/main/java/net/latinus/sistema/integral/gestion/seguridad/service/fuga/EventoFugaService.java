package net.latinus.sistema.integral.gestion.seguridad.service.fuga;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface EventoFugaService {

    /**
     * Devuelve una lista paginada de eventos de fuga relacionados con un adolescente.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<EventoFugaDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos EventoFugaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EventoFugaDTO>> obtenerFugas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtiene un evento de fuga por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param tokenIdentificador String identificador único de objecto a consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del EventoFuga.
     */
    RespuestaPorDefectoAuditoria<EventoFugaDTO> obtenerFugaPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);


    /**
     * Crea un nuevo evento de fuga.
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del EventoFuga registrado.
     */
    RespuestaPorDefectoAuditoria<EventoFugaDTO> crearFuga(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crear o editar un borrador de fuga
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     *
     * @return RespuestaPorDefectoAuditoria<TrasladoDTO>, Devuelve respuesta para auditoria  objeto TrasladoDTO creado o editado
     */
    RespuestaPorDefectoAuditoria<EventoFugaDTO> guardarBorrador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina un evento de fuga por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ID del evento de fuga (encriptado).
     * @return RespuestaPorDefectoAuditoria con el estado de eliminación.
     */
//    RespuestaPorDefectoAuditoria<EventoFugaDTO> eliminarFuga(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    RespuestaPorDefectoAuditoria<Boolean> eliminarFuga(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Obtiene eventos de fuga relacionados con una ficha de identificación.
     *
     * @param httpServletRequest HttpServletRequest para validar la solicitud.
     * @param bodyEncriptado     BodyEncriptado que contiene el ID de ficha de identificación.
     * @return RespuestaPorDefectoAuditoria con la lista de eventos de fuga.
     */
    RespuestaPorDefectoAuditoria<List<EventoFugaDTO>> obtenerFugasPorFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


}
