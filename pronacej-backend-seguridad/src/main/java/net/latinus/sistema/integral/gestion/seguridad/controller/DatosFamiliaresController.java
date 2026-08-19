package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.DatosFamiliaresService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.DatosFamiliaresDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.DatosFamiliaresDocumentoService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/datosFamiliares")
@SecurityRequirement(name = "Authorization")
public class DatosFamiliaresController {

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private DatosFamiliaresDocumentoService datosFamiliaresDocumentoService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    private DatosFamiliaresService datosFamiliaresService;

@PostMapping("/crearDatosFamiliares")
@Operation(summary = "Crea o edita los datos familiares asociados a la ficha")
public ResponseEntity<BodyEncriptado> crearDatosFamiliares(HttpServletRequest httpServletRequest,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

    Date fechaRequest = new Date();
    String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

    // Deserializar el DTO para determinar si es creación o edición
    DatosFamiliaresDTO datosFamiliaresDTO = new Gson().fromJson(body, DatosFamiliaresDTO.class);

    RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> df = this.datosFamiliaresService.crearDatosFamiliares(httpServletRequest, datosFamiliaresDTO);

    // Determinar la acción de auditoría basada en el campo esEdicion del frontend
    String accionAuditoria;
    boolean esEdicion = datosFamiliaresDTO.getEsEdicion() != null && datosFamiliaresDTO.getEsEdicion();
    System.out.println("Variable edicion: " + esEdicion);
    if (esEdicion) {
        // Si esEdicion es true, es una edición
        accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_DATOS_FAMILIARES;
    } else {
        // Si esEdicion es false o null, es una creación
        accionAuditoria = EtiquetaNemonico.ACCION_CREAR_DATOS_FAMILIARES;
    }

    this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
            fechaRequest, accionAuditoria);

    return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
}

    @PostMapping("/obtenerDatosFamiliares")
    @Operation(summary = "Obtiene los datos familiares de una ficha segun su tokenIdentificador")
    public ResponseEntity<BodyEncriptado> obtenerDatosFamiliares(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> df = this.datosFamiliaresService.obtenerDatosFamiliaresToken(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_DATOS_FAMILIARES
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de datos familiares")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.datosFamiliaresDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_DATOS_FAMILIARES_SUBIDA_DE_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados al registro de datos familiares")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.datosFamiliaresDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, DatosFamiliaresDocumentosRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_DATOS_FAMILIARES_OBTENCION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumento")
    @Operation(summary = "Eliminar documentos asociados a detalle")
    public ResponseEntity<BodyEncriptado> eliminarDocumento(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        DatosFamiliaresDocumentoDTO datosFamiliaresDocumentoDTO = new Gson().fromJson(bodyDesencriptado, DatosFamiliaresDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<DatosFamiliaresDocumentoDTO> df = this.datosFamiliaresDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, datosFamiliaresDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_DATOS_FAMILIARES_ELIMINACION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/subirDocumentoFichaPsicosocial")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de datos familiares")
    public ResponseEntity<BodyEncriptado> subirDocumentoFichaPsicosocial(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.datosFamiliaresDocumentoService.subirDocumentoFichaPsicoSocial(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_DATOS_FAMILIARES_SUBIDA_DE_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentosFichaPsicosocial")
    @Operation(summary = "Obtiene todos los documentos asociados al registro de datos familiares")
    public ResponseEntity<BodyEncriptado> obtenerDocumentosFichaPsicosocial(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.datosFamiliaresDocumentoService.obtenerDocumentosFichaPsicoSocial(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, DatosFamiliaresDocumentosRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_DATOS_FAMILIARES_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
