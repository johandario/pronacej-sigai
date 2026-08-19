package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaAsistenciaPostEgresoDocumento;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaAsistenciaPostEgresoDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PertenenciaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.PlanAsistenciaPostEgresoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.DetalleFichaAsistenciaPostEgresoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.FichaAsistenciaPostEgresoDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.FichaAsistenciaPostEgresoService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/planes-asistencia-post-egreso")
@SecurityRequirement(name = "Authorization")
public class PlanAsistenciaPostEgresoController {
    private PlanAsistenciaPostEgresoService planAsistenciaPostEgresoService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private FichaAsistenciaPostEgresoService fichaAsistenciaPostEgresoService;
    private DetalleFichaAsistenciaPostEgresoService detalleFichaAsistenciaPostEgresoService;
    private FichaAsistenciaPostEgresoDocumentoService fichaAsistenciaPostEgresoDocumentoService;

    @PostMapping("/lista")
    @Operation(summary = "Obtener lista de registros de planes de asistencia post egreso")
    public ResponseEntity<BodyEncriptado> obtenerPlanTratamiento(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanAsistenciaPostEgresoDTO>> df = this.planAsistenciaPostEgresoService.obtenerPlanes(httpServletRequest, bodyEncriptado);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/buscar-por-token")
    @Operation(summary = "Obtener plan de asistencia post egreso por token identificador")
    public ResponseEntity<BodyEncriptado> obtenerPlanPorTokenIdentificador(HttpServletRequest httpServletRequest,
                                                                           @RequestParam String param) throws Exception {
        RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> df = this.planAsistenciaPostEgresoService.obtenerPlanPorTokenIdentificador(httpServletRequest, param);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Creación de plan")
    public ResponseEntity<BodyEncriptado> crearPlanTratamiento(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> df = this.planAsistenciaPostEgresoService.crearPlan(httpServletRequest, bodyEncriptado);
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_PLAN_ASISTENCIA;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PLAN_ASISTENCIA;
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

        RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> df = this.planAsistenciaPostEgresoService.eliminarPlan(httpServletRequest, bodyEncriptado);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_PLAN_ASISTENCIA;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearFichaAsistenciaPostEgreso")
    @Operation(summary = "Crea o edita una ficha de asistencia post egreso")
    public ResponseEntity<BodyEncriptado> crearFichaAsistenciaPostEgreso(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDTO> response = fichaAsistenciaPostEgresoService
                .crearFichaAsistenciaPostEgreso(httpServletRequest, bodyEncriptado);

        BodyEncriptado body = response.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }


    @PostMapping("/obtenerFichasAsistenciaPostEgreso")
    @Operation(summary = "Obtiene una lista paginada de fichas de asistencia post egreso")
    public ResponseEntity<BodyEncriptado> obtenerFichasAsistenciaPostEgreso(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaAsistenciaPostEgresoDTO>> response =
                fichaAsistenciaPostEgresoService.obtenerFichasAsistenciaPostEgreso(httpServletRequest, bodyEncriptado);

        BodyEncriptado body = response.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }


    @PostMapping("/eliminarFichaAsistenciaPostEgreso")
    @Operation(summary = "Elimina una ficha de asistencia post egreso (marca como removido)")
    public ResponseEntity<BodyEncriptado> eliminarFichaAsistenciaPostEgreso(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<Boolean> response = fichaAsistenciaPostEgresoService
                .eliminarFichaAsistenciaPostEgreso(httpServletRequest, bodyEncriptado);

        BodyEncriptado body = response.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDetallesPorFichaAsistencia")
    @Operation(summary = "obtiene un listado detalle de ficha de asistencia post egreso")
    public ResponseEntity<BodyEncriptado> obtenerDetallesPorFichaAsistencia(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO>> response =
                detalleFichaAsistenciaPostEgresoService.obtenerDetallesPorFichaAsistencia(httpServletRequest, bodyEncriptado);

        BodyEncriptado body = response.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }


    @PostMapping("/crearOEditarDetalleFichaAsistencia")
    @Operation(summary = "Crea o edita un detalle de ficha de asistencia post egreso")
    public ResponseEntity<BodyEncriptado> crearOEditarDetalleFichaAsistencia(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<DetalleFichaAsistenciaPostEgresoDTO> response =
                detalleFichaAsistenciaPostEgresoService.crearOEditarDetalleFichaAsistencia(httpServletRequest, bodyEncriptado);

        BodyEncriptado body = response.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }


    @PostMapping("/eliminarDetalleFichaAsistencia")
    @Operation(summary = "Elimina un detalle de ficha de asistencia post egreso (marca como removido)")
    public ResponseEntity<BodyEncriptado> eliminarDetalleFichaAsistencia(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<Boolean> response =
                detalleFichaAsistenciaPostEgresoService.eliminarDetalleFichaAsistencia(httpServletRequest, bodyEncriptado);

        BodyEncriptado body = response.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }

    @PostMapping("/subirDocumentoFichaAsistencia")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de pertenencias")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.fichaAsistenciaPostEgresoDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(),
                df, fechaRequest, "ACCION_USUARIO_PLAN_ASISTENCIA_SEGUIMIENTO_SUBIDA_DE_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentosFichaAsistencia")
    @Operation(summary = "Obten todos los documentos asociados al registro de pertenencias")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.fichaAsistenciaPostEgresoDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, FichaAsistenciaPostEgresoDocumentosRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PLAN_ASISTENCIA_SEGUIMIENTO_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumentoFichaAsistencia")
    @Operation(summary = "Eliminar documentos asociados a detalle")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        FichaAsistenciaPostEgresoDocumentoDTO fichaAsistenciaPostEgresoDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaAsistenciaPostEgresoDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDocumentoDTO> df = this.fichaAsistenciaPostEgresoDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, fichaAsistenciaPostEgresoDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PLAN_ASISTENCIA_SEGUIMIENTO_ELIMINACION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
