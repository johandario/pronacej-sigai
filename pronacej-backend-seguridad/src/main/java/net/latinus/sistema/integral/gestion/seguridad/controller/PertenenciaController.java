package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PertenenciaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.PertenenciaDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.PertenenciaService;
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
@RequestMapping(path = "api/v1/pertenencias")
@SecurityRequirement(name = "Authorization")
public class PertenenciaController {
    private PertenenciaService pertenenciaService;
    private PertenenciaDocumentoService pertenenciaDocumentoService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/lista")
    @Operation(summary = "Obtener registro ingreso/salida de pertenencias")
    public ResponseEntity<BodyEncriptado> obtenerPertenenciasEncryp(HttpServletRequest httpServletRequest,
                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<PertenenciaDTO>> df = this.pertenenciaService.obtenerPertenenciasEncrypt(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PERTENENCIA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> buscarPertenenciaPorId(HttpServletRequest httpServletRequest,
                                                                 @RequestParam Long param) throws Exception {
        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<PertenenciaDTO> df = this.pertenenciaService.obtenerPertenenciasPorId(httpServletRequest, param);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                String.valueOf(param),
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_LISTAR_PERTENENCIA
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Creación de expediente")
    public ResponseEntity<BodyEncriptado> crearPertenencia(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        PertenenciaDTO pertenenciaDTO = new Gson().fromJson(bodyDesencriptado, PertenenciaDTO.class);

        RespuestaPorDefectoAuditoria<PertenenciaDTO> df = this.pertenenciaService.crearPertenencia(httpServletRequest, pertenenciaDTO);
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_LISTAR_PERTENENCIA;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PERTENENCIA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminado lógico de expediente")
    public ResponseEntity<BodyEncriptado> eliminarPertenencia(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        PertenenciaDTO pertenenciaDTO = new Gson().fromJson(bodyDesencriptado, PertenenciaDTO.class);

        RespuestaPorDefectoAuditoria<PertenenciaDTO> df = this.pertenenciaService.eliminarPertenencia(httpServletRequest, pertenenciaDTO);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_PERTENENCIA;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de pertenencias")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.pertenenciaDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(),
                df, fechaRequest, "ACCION_USUARIO_PERTENENCIAS_SUBIDA_DE_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obten todos los documentos asociados al registro de pertenencias")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.pertenenciaDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, PertenenciaDocumentosRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PERTENENCIAS_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumento")
    @Operation(summary = "Eliminar documentos asociados a detalle")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        PertenenciaDocumentoDTO pertenenciaDocumentoDTO = new Gson().fromJson(bodyDesencriptado, PertenenciaDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<PertenenciaDocumentoDTO> df = this.pertenenciaDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, pertenenciaDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_PERTENENCIAS_ELIMINACION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

}
