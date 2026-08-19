package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.FichaIngresoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.FichaIngresoDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.FichaIngresoService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ficha-ingreso")
@SecurityRequirement(name = "Authorization")
public class FichaIngresoController {

    private FichaIngresoService fichaIngresoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private FichaIngresoDocumentoService fichaIngresoDocumentoService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerFichasIngresoPaginado")
    @Operation(summary = "Obtiene las fichas de ingreso válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerFichasIngresoPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIngresoDTO>> df = this.fichaIngresoService.obtenerFichasIngreso(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FICHA_INGRESO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarFichaIngreso")
    @Operation(summary = "Elimina una ficha de ingreso")
    public ResponseEntity<BodyEncriptado> eliminarFichaIngreso(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.fichaIngresoService.eliminarFichaIngreso(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_FICHA_INGRESO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearFichaIngreso")
    @Operation(summary = "Crea o edita una ficha de ingreso")
    public ResponseEntity<BodyEncriptado> crearFichaIngreso(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        FichaIngresoDTO fichaIngresoDTO = new Gson().fromJson(body, FichaIngresoDTO.class);

        // Determinar si es creación o edición
        boolean esEdicion = false;
        
        // Verificar si tiene tokenIdentificador válido (no nulo y no "0")
        if (fichaIngresoDTO.getTokenIdentificador() != null && 
            !fichaIngresoDTO.getTokenIdentificador().equals("0") && 
            !fichaIngresoDTO.getTokenIdentificador().trim().isEmpty()) {
            esEdicion = true;
        }
        
        // También verificar el campo esEdicion si está disponible
        if (fichaIngresoDTO.getEsEdicion() != null && fichaIngresoDTO.getEsEdicion()) {
            esEdicion = true;
        }

        RespuestaPorDefectoAuditoria<FichaIngresoDTO> df = this.fichaIngresoService.crearFichaIngreso(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición o creación
        String accionAuditoria;
        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_FICHA_INGRESO;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_FICHA_INGRESO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarFicha")
    @Operation(summary = "Actualiza una ficha de identificacion")
    public ResponseEntity<BodyEncriptado> actualizarFicha(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = this.fichaIngresoService.actualizarFicha(httpServletRequest, new Gson().fromJson(body, FichaIdentificacionDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_FICHA_INGRESO
                );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/removerFicha")
    @Operation(summary = "Remueve una ficha de identificacion")
    public ResponseEntity<BodyEncriptado> removerFicha(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = this.fichaIngresoService.removerFicha(httpServletRequest, new Gson().fromJson(body, FichaIdentificacionDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_FICHA_INGRESO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerIngresoPorFichaPrincipal")
    @Operation(summary = "Obtener último ingreso de ficha de identificación")
    public ResponseEntity<BodyEncriptado> obtenerUltimoIngresoValidoPorTokenFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        RespuestaPorDefectoAuditoria<FichaIngresoDTO> df = this.fichaIngresoService.obtenerUltimoIngresoValidoPorTokenFichaIdentificacion(httpServletRequest, bodyEncriptado);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirDocumentoFichaIngreso")
    @Operation(summary = "Sube un documento y lo asocia a una ficha de ingreso")
    public ResponseEntity<BodyEncriptado> subirDocumentoFICHA(HttpServletRequest httpServletRequest,
                                                              @RequestParam("documento") MultipartFile multipartFile,
                                                              @RequestParam("body") String bodyEncriptadoString) throws Exception {
        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);

        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.fichaIngresoDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHAINGRESO_SUBIDA_DE_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentosFichaIngreso")
    @Operation(summary = "Obtiene todos los documentos asociados a una ficha de ingreso")
    public ResponseEntity<BodyEncriptado> obtenerDocumentosFICHA(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        FichaIngresoDocumentoRequest requestObj = new Gson().fromJson(bodyDesencriptado, FichaIngresoDocumentoRequest.class);

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df =
                this.fichaIngresoDocumentoService.obtenerDocumentos(httpServletRequest, requestObj);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHAINGRESO_OBTENCION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumentoFichaIngreso")
    @Operation(summary = "Elimina la relación entre una ficha de ingreso y un documento")
    public ResponseEntity<BodyEncriptado> eliminarDocumentoFICHA(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        FichaIngresoDocumentoDTO fichaIngresoDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaIngresoDocumentoDTO.class);

        RespuestaPorDefectoAuditoria<FichaIngresoDocumentoDTO> df = this.fichaIngresoDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, fichaIngresoDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHAINGRESO_ELIMINACION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerTodosDocumentosFichaIngreso")
    @Operation(summary = "Obtiene todos los documentos asociados a una ficha de ingreso")
    public ResponseEntity<BodyEncriptado> obtenerTodosDocumentosFICHA(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        FichaIngresoDocumentoRequest requestObj = new Gson().fromJson(bodyDesencriptado, FichaIngresoDocumentoRequest.class);

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df =
                this.fichaIngresoDocumentoService.obtenerDocumentosFichaIngreso(httpServletRequest, requestObj);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHAINGRESO_OBTENCION_TODOS_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}