package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ContactoAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import java.util.List;

public interface ContactoAdolescenteService {
    /**
     * Devuelve una lista paginada de los contactos.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     Datos para la paginación (ej. ID de ficha, rango de fechas, etc.).
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ContactoAdolescenteDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ContactoAdolescenteDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<ContactoAdolescenteDTO>> obtenerContactos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtiene un  contacto por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param tokenIdentificador String identificador único de objecto a consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del contacto.
     */
    RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> obtenerContactosPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);


    /**
     * Crea un nuevo  contacto.
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado objecto a crear o consultar.
     * @return RespuestaPorDefectoAuditoria con los datos del contacto.
     */
    RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> crearContacto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);



    /**
     * Elimina un contacto por su ID.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado     ID del registro (encriptado).
     * @return RespuestaPorDefectoAuditoria con el estado de eliminación.
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarContactos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve todas los contactos sin aplicar paginación.
     *
     * @param httpServletRequest Request HTTP.
     * @return RespuestaPorDefectoAuditoria con la lista de todas los contactos.
     */
    RespuestaPorDefectoAuditoria<List<ContactoAdolescenteDTO>> obtenerTodasLasContactos(HttpServletRequest httpServletRequest);


}
