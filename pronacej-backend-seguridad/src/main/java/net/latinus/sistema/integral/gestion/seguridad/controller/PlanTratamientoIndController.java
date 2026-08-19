package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PertenenciaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PlanTratamientoIndSeguiAbiertoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PlanTratamientoIndSeguiDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.*;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/planes-tratamiento")
@SecurityRequirement(name = "Authorization")
public class PlanTratamientoIndController {
    private PlanTratamientoIndService planTratamientoIndService;
    private PlanTratamientoIndSeguiService planTratamientoIndSeguiService;
    private PlanTratamientoIndIntervSeguiService planTratamientoIndIntervSeguiService;
    private PlanTratamientoIndIntervService planTratamientoIndIntervService;
    private ActividadIntervencionService actividadIntervencionService;
    private ActividadIntervencionSeguimientoService actividadIntervencionSeguimientoService;
    private PlanTratamientoIndSeguiAbiertoService planTratamientoIndSeguiAbiertoService;
    private PlanTratamientoIndSeguiAbiertoDocumentoService planTratamientoIndSeguiAbiertoDocumentoService;
    private PlanTratamientoIndSeguiDocumentoService planTratamientoIndSeguiDocumentoService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/lista")
    @Operation(summary = "Obtener lista de registros de planes de tratamiento individual")
    public ResponseEntity<BodyEncriptado> obtenerPlanTratamiento(HttpServletRequest httpServletRequest,
                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndDTO>> df = this.planTratamientoIndService.obtenerPlanes(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> buscarPlanPorId(HttpServletRequest httpServletRequest,
                                                                 @RequestParam Long param) throws Exception {
        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = this.planTratamientoIndService.obtenerPlanPorId(httpServletRequest, param);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                String.valueOf(param), df, fechaInicio, EtiquetaNemonico.ACCION_EDITAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/buscar-plan-activo")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> buscarPlanActivo(HttpServletRequest httpServletRequest,
                                                          @RequestParam String param) throws Exception {
        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = this.planTratamientoIndService.obtenerPlanActivo(httpServletRequest, param);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                String.valueOf(param), df, fechaInicio, EtiquetaNemonico.ACCION_EDITAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Creación de plan")
    public ResponseEntity<BodyEncriptado> crearPlanTratamiento(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = this.planTratamientoIndService.crearPlan(httpServletRequest, bodyEncriptado);
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_PLAN;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PLAN;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminación de plan")
    public ResponseEntity<BodyEncriptado> eliminarPlanTratamiento(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = this.planTratamientoIndService.eliminarPlan(httpServletRequest, bodyEncriptado);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_PLAN;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/lista-seguimiento")
    @Operation(summary = "Obtener lista de registros de seguimientos")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientos(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndIntervSeguiDTO>> df = this.planTratamientoIndIntervSeguiService.obtenerSeguimientos(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear-seguimiento")
    @Operation(summary = "Creación de seguimiento")
    public ResponseEntity<BodyEncriptado> crearSeguimiento(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> df = this.planTratamientoIndIntervSeguiService.crearSeguimiento(httpServletRequest, bodyEncriptado);
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_PLAN_SEGUIMIENTO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PLAN_SEGUIMIENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar-seguimiento")
    @Operation(summary = "Eliminación de seguimiento")
    public ResponseEntity<BodyEncriptado> eliminaSeguimiento(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> df = this.planTratamientoIndIntervSeguiService.eliminarSeguimiento(httpServletRequest, bodyEncriptado);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_PLAN_SEGUIMIENTO;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtener-intervencion")
    @Operation(summary = "Obtener una intervencion segun el tokenIdentificador")
    public ResponseEntity<BodyEncriptado> getIntervencionByTokenIdentificador(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> df = this.planTratamientoIndIntervService.getByTokenIdentificador(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizar-intervencion")
    @Operation(summary = "Actualizar una intervencion segun el idIntervencion")
    public ResponseEntity<BodyEncriptado> updatePlanTratamientoIndInterv(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> df = this.planTratamientoIndIntervService.updatePlanTratamientoIndInterv(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_EDITAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/lista-actividades")
    @Operation(summary = "Obtener lista paginada de actividades de intervención por idPlanTratIndInterv")
    public ResponseEntity<BodyEncriptado> obtenerActividades(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionDTO>> df =
                this.actividadIntervencionService.obtenerActividadesIntervencionPorIdPlanTratIndInterv(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear-actividad")
    @Operation(summary = "Crear o actualizar actividad de intervención")
    public ResponseEntity<BodyEncriptado> crearActualizarActividad(HttpServletRequest httpServletRequest,
                                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> df =
                this.actividadIntervencionService.crearActualizarActividadIntervencion(httpServletRequest, bodyEncriptado);

        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_ACTIVIDAD_INTERVENCION;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_ACTIVIDAD_INTERVENCION;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/lista-seguimiento-actividad")
    @Operation(summary = "Obtener lista paginada de seguimientos por idActividadIntervencion")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientosActividad(HttpServletRequest httpServletRequest,
                                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionSeguimientoDTO>> df =
                this.actividadIntervencionSeguimientoService.obtenerSeguimientosPorActividadId(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear-seguimiento-actividad")
    @Operation(summary = "Crear o actualizar seguimiento de intervención")
    public ResponseEntity<BodyEncriptado> crearActualizarSeguimientoActividad(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<ActividadIntervencionSeguimientoDTO> df =
                this.actividadIntervencionSeguimientoService.crearActualizarActividadIntervencionSeguimiento(httpServletRequest, bodyEncriptado);

        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_ACTIVIDAD_SEGUIMIENTO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_ACTIVIDAD_SEGUIMIENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtener-actividad-por-id")
    @Operation(summary = "Obtener información de una ActividadIntervencion por su idActividadIntervencion")
    public ResponseEntity<BodyEncriptado> obtenerActividadPorId(HttpServletRequest request,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> respuesta =
                actividadIntervencionService.getActividadIntervencionPorId(request, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(request,
                bodyDesencriptado, respuesta, fechaInicio, EtiquetaNemonico.ACCION_EDITAR_PLAN);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar-actividad")
    @Operation(summary = "Elimina una ActividadIntervencion")
    public ResponseEntity<BodyEncriptado> eliminarActividadIntervencion(HttpServletRequest request,
                                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> respuesta =
                actividadIntervencionService.eliminarActividadIntervencion(request, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(request,
                bodyDesencriptado,respuesta , fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PLAN);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/lista-seguimiento-pti")
    @Operation(summary = "Obtener lista de registros de seguimientos de planes de tratamiento individual")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientosPlanTratamiento(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiDTO>> df = this.planTratamientoIndSeguiService.obtenerSeguimientos(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear-seguimiento-pti")
    @Operation(summary = "Creación de seguimiento")
    public ResponseEntity<BodyEncriptado> crearSeguimientoPlanTratamiento(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> df = this.planTratamientoIndSeguiService.crearSeguimiento(httpServletRequest, bodyEncriptado);
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_PLAN_SEGUIMIENTO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PLAN_SEGUIMIENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar-seguimiento-pti")
    @Operation(summary = "Eliminación de seguimiento")
    public ResponseEntity<BodyEncriptado> eliminarSeguimientoPlanTratamiento(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> df = this.planTratamientoIndSeguiService.eliminarSeguimiento(httpServletRequest, bodyEncriptado);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_PLAN_SEGUIMIENTO;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/lista-seguimientos-abierto")
    @Operation(summary = "Obtener lista de registros de fichas de seguimiento de pti regimen abierto")
    public ResponseEntity<BodyEncriptado> obtenerFichasSeguimientoAbierto(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>> df = this.planTratamientoIndSeguiAbiertoService.obtenerFichasSeguimientoAbierto(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PLAN);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear-ficha-seguimiento-pti-abierto")
    @Operation(summary = "Creación de ficha de seguimiento abierto")
    public ResponseEntity<BodyEncriptado> crearFichaSeguimientoAbierto(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> df = this.planTratamientoIndSeguiAbiertoService.crearEditarFichaSeguimientoAbierto(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_CREAR_PLAN_SEGUIMIENTO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar-ficha-seguimiento-pti-abierto")
    @Operation(summary = "Eliminación de ficha de seguimiento abierto")
    public ResponseEntity<BodyEncriptado> eliminarFichaSeguimientoAbierto(HttpServletRequest httpServletRequest,
                                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> df = this.planTratamientoIndSeguiAbiertoService.eliminarFichaSeguimientoAbierto(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PLAN_SEGUIMIENTO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirDocumentoFichaSeguimientoAbierto")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de ficha de seguimiento")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.planTratamientoIndSeguiAbiertoDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(),
                df, fechaRequest, "ACCION_USUARIO_PLAN_TRATAMIENTO_SEGUI_ABIERTO_SUBIDA_DE_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentosFichaSeguimientoAbierto")
    @Operation(summary = "Obten todos los documentos asociados al registro de ficha de seguimiento")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.planTratamientoIndSeguiAbiertoDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, PlanTratamientoIndSeguiAbiertoDocumentoRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PLAN_TRATAMIENTO_SEGUI_ABIERTO_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumentoFichaSeguimientoAbierto")
    @Operation(summary = "Eliminar documentos asociados a ficha de seguimiento")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        PlanTratamientoIndSeguiAbiertoDocumentoDTO planTratamientoIndSeguiAbiertoDocumentoDTO = new Gson().fromJson(bodyDesencriptado, PlanTratamientoIndSeguiAbiertoDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDocumentoDTO> df = this.planTratamientoIndSeguiAbiertoDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, planTratamientoIndSeguiAbiertoDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PLAN_TRATAMIENTO_SEGUI_ABIERTO_ELIMINACION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/subirDocumentoSeguimiento")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de seguimiento")
    public ResponseEntity<BodyEncriptado> subirDocumentoSeguimiento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.planTratamientoIndSeguiDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(),
                df, fechaRequest, "ACCION_USUARIO_PLAN_TRATAMIENTO_SEGUI_SUBIDA_DE_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentosSeguimiento")
    @Operation(summary = "Obten todos los documentos asociados al registro de seguimiento")
    public ResponseEntity<BodyEncriptado> obtenerDocumentosSeguimiento(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.planTratamientoIndSeguiDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, PlanTratamientoIndSeguiDocumentoRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PLAN_TRATAMIENTO_SEGUI_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumentoSeguimiento")
    @Operation(summary = "Eliminar documentos asociados al seguimiento")
    public ResponseEntity<BodyEncriptado> eliminarDocumentoSeguimiento(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        PlanTratamientoIndSeguiDocumentoDTO planTratamientoIndSeguiDocumentoDTO = new Gson().fromJson(bodyDesencriptado, PlanTratamientoIndSeguiDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDocumentoDTO> df = this.planTratamientoIndSeguiDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, planTratamientoIndSeguiDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PLAN_TRATAMIENTO_SEGUI_ELIMINACION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
