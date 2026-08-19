package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.InstanciaProcesoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.ProcesoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.TareaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.flujo.FlujoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/motor-flujo")
@SecurityRequirement(name = "Authorization")
public class FlujoController {

    private FlujoService flujoService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    // ========== ENDPOINTS DE PROCESOS ==========

    @PostMapping("/proceso/lista")
    @Operation(summary = "Obtener lista procesos")
    public ResponseEntity<BodyEncriptado> obtenerProcesos(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>> df = this.flujoService.obtenerProcesos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PROCESO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/proceso/buscar")
    @Operation(summary = "Obtener proceso por Token ID")
    public ResponseEntity<BodyEncriptado> obtenerProcesoPorTokenID(HttpServletRequest httpServletRequest,
                                                          @RequestParam String ID) throws Exception {

        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<ProcesoDTO> df = this.flujoService.obtenerProcesoPorTokenID(httpServletRequest, ID);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "ID: " + ID, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PROCESO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/proceso/crear")
    @Operation(summary = "Crear proceso")
    public ResponseEntity<BodyEncriptado> crearProceso(HttpServletRequest httpServletRequest,
                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<ProcesoDTO> df = this.flujoService.crearProceso(httpServletRequest, bodyEncriptado);

        // Determinar acción de auditoría basada en si es edición o creación
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_PROCESO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PROCESO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/proceso/eliminar")
    @Operation(summary = "Eliminar proceso")
    public ResponseEntity<BodyEncriptado> eliminarProceso(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<ProcesoDTO> df = this.flujoService.eliminarProceso(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PROCESO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Crear una instancia proceso a partir de un proceso, es importante enviar el nemónico del proceso dentro
     */
    @PostMapping("/instancia-proceso/crear")
    @Operation(summary = "Crear instancia proceso por proceso")
    public ResponseEntity<BodyEncriptado> crearInstanciaProcesoPorProceso(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> df = this.flujoService.crearInstanciaProcesoPorProceso(httpServletRequest, bodyEncriptado);

        // Crear instancia de proceso es crear un proceso en ejecución
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_CREAR_PROCESO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Eliminar una instancia proceso a partir de una tarea relacionada
     */
    @PostMapping("/instancia-proceso/eliminar-por-tarea")
    @Operation(summary = "Eliminar instancia proceso por tarea")
    public ResponseEntity<BodyEncriptado> eliminarInstanciaProcesoPorTarea(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> df = this.flujoService.eliminarInstanciaProcesoPorTarea(httpServletRequest, bodyEncriptado);

        // Eliminar instancia de proceso es eliminar un proceso en ejecución
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PROCESO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    // ========== ENDPOINTS DE TAREAS ==========

    /**
     * Obtener todas las tareas
     */
    @PostMapping("/tareas/lista")
    @Operation(summary = "Obtener lista tareas")
    public ResponseEntity<BodyEncriptado> obtenerTareas(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = this.flujoService.obtenerTareas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtener todas las tareas recibidas
     */
    @PostMapping("/tareas/lista-recibidas")
    @Operation(summary = "Obtener lista tareas recibidas")
    public ResponseEntity<BodyEncriptado> obtenerTareasRecibidas(HttpServletRequest httpServletRequest,
                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = this.flujoService.obtenerTareasRecibidas(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtener todas las tareas enviadas
     */
    @PostMapping("/tareas/lista-enviadas")
    @Operation(summary = "Obtener lista tareas enviadas")
    public ResponseEntity<BodyEncriptado> obtenerTareasEnviadas(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = this.flujoService.obtenerTareasEnviadas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtener todas las tareas borrador
     */
    @PostMapping("/tareas/lista-borrador")
    @Operation(summary = "Obtener lista tareas borrador")
    public ResponseEntity<BodyEncriptado> obtenerTareasBorrador(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = this.flujoService.obtenerTareasBorrador(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/tareas/lista-enviadas/tipo")
    @Operation(summary = "Obtener lista tareas enviadas por tipo")
    public ResponseEntity<BodyEncriptado> obtenerTareasEnviadasPorUsuarioRolYTipo(HttpServletRequest httpServletRequest,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = this.flujoService.obtenerTareasEnviadasPorUsuarioRolYTipo(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtener tipos de tareas
     */
    @PostMapping("/tareas/tipos")
    @Operation(summary = "Obtener tipos de tareas")
    public ResponseEntity<BodyEncriptado> obtenerTipoTareasEnviadasPorUsuarioRol(HttpServletRequest httpServletRequest,
                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<List<String>> df = this.flujoService.obtenerTipoTareasEnviadasPorUsuarioRol(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/tareas/tareaPorTarea")
    @Operation(summary = "Obtener tareas de un flujo basándose en un tarea que pertenece a ese flujo")
    public ResponseEntity<BodyEncriptado> obtenerTareasAsociadasATarea(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<List<TareaDTO>> df = this.flujoService.obtenerTareasInstanciaProcesoPorTarea(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TAREA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}