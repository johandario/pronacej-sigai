package net.latinus.sistema.integral.gestion.seguridad.service.institucion;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.SeguimientoAdolescInstDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import java.util.List;

public interface SeguimientoAdolescInsService {
    /**
     * Devuelve una lista paginada del Registro de instituciones.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroInstitucionDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos RegistroSalidaDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoAdolescInstDTO>> obtenerInstituciones(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtiene un Registro de instituciones por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param tokenIdentificador String identificador único de objecto a consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del Registro de Salida.
     */
    RespuestaPorDefectoAuditoria<SeguimientoAdolescInstDTO> obtenerRegistroInstitucionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);


    /**
     * Crea un nuevo Registro de Institucion.
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del Registro de Institucion.
     */
    RespuestaPorDefectoAuditoria<SeguimientoAdolescInstDTO> crearRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);



    /**
     * Elimina un Registro de Institucion por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ID del registro (encriptado).
     * @return RespuestaPorDefectoAuditoria con el estado de eliminación.
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve todas las instituciones sin aplicar paginación.
     *
     * @param httpServletRequest Request HTTP.
     * @return RespuestaPorDefectoAuditoria con la lista de todas las instituciones.
     */
    RespuestaPorDefectoAuditoria<List<SeguimientoAdolescInstDTO>> obtenerTodasLasInstituciones(HttpServletRequest httpServletRequest);




}
