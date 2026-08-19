package net.latinus.sistema.integral.gestion.seguridad.service.flujo;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.InstanciaProceso;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Paso;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Proceso;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Tarea;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.InstanciaProcesoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.ProcesoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.TareaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface FlujoService {

    /**
     * Obtener todos los procesos de flujos definidos
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ProcesoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>> obtenerProcesos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener proceso por token identificador
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param tokenIdentificador String parámetro de consulta para búsqueda
     *
     * @return RespuestaPorDefectoAuditoria<ProcesoDTO>, Devuelve respuesta para auditoria con objeto ProcesoDTO a buscar
     */
    RespuestaPorDefectoAuditoria<ProcesoDTO> obtenerProcesoPorTokenID(HttpServletRequest httpServletRequest, String tokenIdentificador);

    /**
     * Creación de proceso a partir de un objeto procesoDTO
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado bodyencriptado objeto a ser creado.
     *
     * @return RespuestaPorDefectoAuditoria<ProcesoDTO>, Devuelve respuesta para auditoria con objeto ProcesoDTO creado
     */
    RespuestaPorDefectoAuditoria<ProcesoDTO> crearProceso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Eliminado lógico de proceso a partir de un objeto procesoDTO
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado bodyencriptado objeto a ser eliminado.
     *
     * @return RespuestaPorDefectoAuditoria<ProcesoDTO>, Devuelve respuesta para auditoria con objeto ProcesoDTO eliminado
     */
    RespuestaPorDefectoAuditoria<ProcesoDTO> eliminarProceso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Crear una InstanciaProceso a partir de un ProcesoDTO de entrada
     * @param tokenIdentificadorProceso provee métodos para acceder a los parámetros de una petición
     * @return
     */
    //RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> crearInstancia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    InstanciaProceso crearInstancia(String tokenIdentificadorProceso);

    /**
     * Crear una instancia de proceso por medio de un proceso enviado
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado proceso enviado.
     *
     * @return RespuestaPorDefectoAuditoria<InstanciaProcesoDTO>, Devuelve respuesta de InstanciaProcesoDTO creada
     */
    RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> crearInstanciaProcesoPorProceso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Obtener todos las tareas de flujos definidos
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ProcesoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener todos las tareas de flujos definidos
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ProcesoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasEnviadas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasEnviadasPorUsuarioRolYTipo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<List<String>> obtenerTipoTareasEnviadasPorUsuarioRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    /**
     * Obtener todos las tareas de flujos definidos
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ProcesoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasRecibidas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Obtener todos las tareas entrantes que sean borrador
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>>, Devuelve respuesta para auditoria con lista paginada de objetos ProcesoDTO
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasBorrador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    Tarea completarTareaActualEIniciarSiguiente(BodyJwtValido jwt, InstanciaProceso instanciaProceso, String tokenIdDocumento, String html);

    Tarea rechazarTareaActual(BodyJwtValido jwt, InstanciaProceso instancia, String tokenIdDocumento);

    /**
     * Obtener todos las tareas de un flujo de acuerdo a una tarea
     *
     * @param httpServletRequest httpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado PaginacionRequest dato que continene información de paginador de petición.
     *
     * @return RespuestaPorDefectoAuditoria<List<TareaDTO>>, Devuelve respuesta para auditoria con lista de objetos TareaDTO
     */
    RespuestaPorDefectoAuditoria<List<TareaDTO>> obtenerTareasInstanciaProcesoPorTarea(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina una instancia proceso mediante una tarea relacionada
     *
     * @param httpServletRequest HttpServletRequest provee métodos para acceder a los parámetros de una petición.
     * @param bodyEncriptado BodyEncriptado dato que continene el objeto TareaDTO asociado a la instancia a borrar.
     *
     * @return RespuestaPorDefectoAuditoria<InstanciaProcesoDTO>, Devuelve respuesta para auditoria con la IntanciaProcesoDTO eliminado
     */
    RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> eliminarInstanciaProcesoPorTarea(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


}
