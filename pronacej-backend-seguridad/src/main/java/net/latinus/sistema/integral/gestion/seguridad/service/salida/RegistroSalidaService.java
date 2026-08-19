package net.latinus.sistema.integral.gestion.seguridad.service.salida;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;


public interface RegistroSalidaService {
    /**
     * Devuelve una lista paginada del Registro de Salida.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos RegistroSalidaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> obtenerSalidas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve una lista paginada del Registro de Salida.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos RegistroSalidaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> obtenerlistadoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Obtiene un Registro de Salida por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param tokenIdentificador String identificador único de objecto a consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del Registro de Salida.
     */
    RespuestaPorDefectoAuditoria<RegistroSalidaDTO> obtenerRegistroSalidaPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);


    /**
     * Crea un nuevo Registro de Salida.
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del Registro de Salida.
     */
    RespuestaPorDefectoAuditoria<RegistroSalidaDTO> crearRegistroSalida(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);



    /**
     * Elimina un Registro de Salida por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ID del registro (encriptado).
     * @return RespuestaPorDefectoAuditoria con el estado de eliminación.
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarRegistroSalida(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve una lista paginada del Registro de Salida.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos RegistroSalidaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> obtenerlistadoFugasTrasladosCompletados(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
