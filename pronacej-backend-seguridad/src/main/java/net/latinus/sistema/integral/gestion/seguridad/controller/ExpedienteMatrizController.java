package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.ExpedienteMatrizDetalleDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ExpedienteMatrizDetalleDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ExpedienteMatrizService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/expediente-matriz")
@SecurityRequirement(name = "Authorization")
public class ExpedienteMatrizController {
    private ExpedienteMatrizService expedienteMatrizService;
    private ExpedienteMatrizDetalleDocumentoService expedienteMatrizDetalleDocumentoService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/lista")
    @Operation(summary = "Obten todos los expedientes")
    public ResponseEntity<BodyEncriptado> obtenerExpedientes(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> df = this.expedienteMatrizService.obtenerExpedientes(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_EXPEDIENTE);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Creación de expediente")
    public ResponseEntity<BodyEncriptado> crearExpediente(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        ExpedienteMatrizDTO expedienteMatrizDTO = new Gson().fromJson(bodyDesencriptado, ExpedienteMatrizDTO.class);

        RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df = this.expedienteMatrizService.crearExpediente(httpServletRequest, expedienteMatrizDTO);

        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_EXPEDIENTE;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_EXPEDIENTE;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminado lógico de expediente")
    public ResponseEntity<BodyEncriptado> eliminarExpediente(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        ExpedienteMatrizDTO expedienteMatrizDTO = new Gson().fromJson(bodyDesencriptado, ExpedienteMatrizDTO.class);

        RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df = this.expedienteMatrizService.eliminarExpediente(httpServletRequest, expedienteMatrizDTO);
        String accion =
                EtiquetaNemonico.ACCION_ELIMINAR_EXPEDIENTE;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de expediente")
    public ResponseEntity<BodyEncriptado> obtenerExpedienteByNum(HttpServletRequest httpServletRequest,
                                                                 @RequestParam String param) throws Exception {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df = this.expedienteMatrizService.obtenerExpedientePorNum(httpServletRequest, param);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar-por-ficha")
    @Operation(summary = "Obten todos los expedientes")
    public ResponseEntity<BodyEncriptado> obtenerExpedientesPorTokenFicha(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado, @RequestParam String param
                                                                          ) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> df = this.expedienteMatrizService.obtenerExpedientePorTokenFicha(httpServletRequest, bodyEncriptado, param);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento y lo asocia al detalle respectivo del expediente")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.expedienteMatrizDetalleDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, "ACCION_USUARIO_MANDATO_EXPEDIENTE_LEGAL_SUBIDA_DE_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obten todos los documentos asocialo al detalle")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.expedienteMatrizDetalleDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, ExpedienteMatrizDetalleDocumentosRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_MANDATO_EXPEDIENTE_LEGAL_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumento")
    @Operation(summary = "Eliminar documentos asociados a detalle")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        ExpedienteMatrizDetalleDocumentoDTO expedienteMatrizDetalleDocumentoDTO = new Gson().fromJson(bodyDesencriptado, ExpedienteMatrizDetalleDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDocumentoDTO> df = this.expedienteMatrizDetalleDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, expedienteMatrizDetalleDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_MANDATO_EXPEDIENTE_LEGAL_ELIMINACION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerEstadisticasDelitos")
    public ResponseEntity<BodyEncriptado>  obtenerEstadisticasDelitos(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado body) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<List<DelitoEstadisticaDTO>> respuesta =
                expedienteMatrizService.obtenerEstadisticasDelitos(httpServletRequest, body);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_REPORTE_DELITOS
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerUltimoExpedienteDetalle")
    public ResponseEntity<BodyEncriptado>  obtenerUltimoExpedienteDetalle(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado body) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> respuesta =
                expedienteMatrizService.obtenerExpedienteDetallePorFicha(httpServletRequest, body);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_DETALLE_EXPEDIENTE
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerExpedienteCabeceraYDetalleActualPorFicha")
    public ResponseEntity<BodyEncriptado>  obtenerExpedienteCabeceraYDetalleActualPorFicha(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado body) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> respuesta =
                expedienteMatrizService.obtenerExpedienteCabeceraYDetalleActualPorFicha(httpServletRequest, body);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_DETALLE_EXPEDIENTE
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


}
