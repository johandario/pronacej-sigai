package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ActividadOcupacionalService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.EvaluacionConductualService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.SeguimientoActividadOcupacionalService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacion-conductual")
@SecurityRequirement(name = "Authorization")
public class EvaluacionConductualController {
    
    private EvaluacionConductualService evaluacionConductualService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ActividadOcupacionalService actividadOcupacionalService;
    private SeguimientoActividadOcupacionalService seguimientoActividadOcupacionalService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerEvaluacionesConductualesPaginado")
    @Operation(summary = "Obtiene las evaluaciones conductuales válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionesConductualesPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionConductualDTO>> df = this.evaluacionConductualService.obtenerEvaluacionesConductuales(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerSituPersCaraPersPaginado")
    @Operation(summary = "Obtiene las situaciones personales válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerSituPersCaraPersPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<SituPersCaraPersDTO>> df = this.evaluacionConductualService.obtenerSituPersCaraPers(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerCondHistViolPaginado")
    @Operation(summary = "Obtiene las conductas e histórico de violencias válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerCondHistViolPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<CondHistViolDTO>> df = this.evaluacionConductualService.obtenerCondHistViol(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarEvaluacionConductual")
    @Operation(summary = "Elimina una evaluación conductual")
    public ResponseEntity<BodyEncriptado> eliminarEvaluacionConductual(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionConductualService.eliminarEvaluacionConductual(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarSituPersCaraPers")
    @Operation(summary = "Elimina una situación personal")
    public ResponseEntity<BodyEncriptado> eliminarSituPersCaraPers(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionConductualService.eliminarSituPersCaraPers(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarCondHistViol")
    @Operation(summary = "Elimina una conducta e histórico de violencias")
    public ResponseEntity<BodyEncriptado> eliminarCondHistViol(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionConductualService.eliminarCondHistViol(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearEvaluacionConductual")
    @Operation(summary = "Crea una evaluación conductual")
    public ResponseEntity<BodyEncriptado> crearEvaluacionConductual(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<EvaluacionConductualDTO> df = this.evaluacionConductualService.crearEvaluacionConductual(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_EVALUACION_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerActividadOcupacional")
    @Operation(summary = "Obtener una actividad ocupacional según su tokenIdentificador")
    public ResponseEntity<BodyEncriptado> obtenerActividadOcupacionalPorToken(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> df =
                actividadOcupacionalService.obtenerActividadOcupacionalPorToken(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ACTIVIDAD_OCUPACIONAL);
        
        BodyEncriptado response = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/crearOActualizarActividadOcupacional")
    @Operation(summary = "Crear o actualizar una actividad ocupacional")
    public ResponseEntity<BodyEncriptado> crearOActualizarActividadOcupacional(HttpServletRequest httpServletRequest,
                                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        ActividadOcupacionalDTO actividadDTO = new Gson().fromJson(body, ActividadOcupacionalDTO.class);

        RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> respuesta =
                actividadOcupacionalService.crearActividadOcupacional(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición o creación
        String accionAuditoria;
        if (actividadDTO.getEsEdicion() != null && actividadDTO.getEsEdicion()) {
            // Si es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_ACTIVIDAD_OCUPACIONAL;
        } else {
            // Si es creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_ACTIVIDAD_OCUPACIONAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, respuesta,
                fechaRequest, accionAuditoria);

        BodyEncriptado responseBody = respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/eliminarActividadOcupacional")
    @Operation(summary = "Marcar una actividad ocupacional como eliminada")
    public ResponseEntity<BodyEncriptado> eliminarActividadOcupacional(HttpServletRequest httpServletRequest,
                                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> respuesta =
                actividadOcupacionalService.eliminarActividadOcupacional(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, respuesta, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_ACTIVIDAD_OCUPACIONAL);
        
        BodyEncriptado responseBody = respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/listarActividadesOcupacionales")
    @Operation(summary = "Listar actividades ocupacionales asociadas a una ficha de identificación con paginación")
    public ResponseEntity<BodyEncriptado> listarActividadesOcupacionales(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadOcupacionalDTO>> respuesta =
                actividadOcupacionalService.obtenerActividadesOcupacionalesPorFicha(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, respuesta, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ACTIVIDAD_OCUPACIONAL);
        
        BodyEncriptado responseBody = respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/crearOEditarSeguimientoActividadOcupacional")
    @Operation(summary = "Crear o Editar Seguimiento de Actividad Ocupacional")
    public ResponseEntity<BodyEncriptado> crearOEditarSeguimiento(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<SeguimientoActividadOcupacionalDTO> respuesta =
                this.seguimientoActividadOcupacionalService.crearSeguimiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, respuesta,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_SEGUIMIENTO_ACTIVIDAD_OCUPACIONAL);

        BodyEncriptado responseBody = respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/listarSeguimientosPorActividad")
    @Operation(summary = "Listar Seguimientos por Actividad Ocupacional")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientosPorActividad(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoActividadOcupacionalDTO>> respuesta =
                this.seguimientoActividadOcupacionalService.obtenerSeguimientosPorActividad(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, respuesta,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_SEGUIMIENTO_ACTIVIDAD_OCUPACIONAL);

        BodyEncriptado responseBody = respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/eliminarSeguimientoActividadOcupacional")
    @Operation(summary = "Eliminar Seguimiento de Actividad Ocupacional")
    public ResponseEntity<BodyEncriptado> eliminarSeguimiento(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<Boolean> respuesta =
                this.seguimientoActividadOcupacionalService.eliminarSeguimiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, respuesta,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_SEGUIMIENTO_ACTIVIDAD_OCUPACIONAL);

        BodyEncriptado responseBody = respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(responseBody);
    }
}