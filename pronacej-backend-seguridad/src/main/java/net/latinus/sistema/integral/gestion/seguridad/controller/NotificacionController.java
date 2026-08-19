package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.NotificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.NotificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/notificacion")
@SecurityRequirement(name = "Authorization")
public class NotificacionController {

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private NotificacionService notificacionService;

    @PostMapping(value = "/obtenerNotificacionesPorToken")
    @Operation(summary = "Obtiene las notificaciones del adolescente")
    public ResponseEntity<BodyEncriptado> obtenerNotificacionesPorToken(HttpServletRequest httpServletRequest,
                                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<NotificacionDTO>> df = this.notificacionService.obtenerNotificacionesPorToken(
                httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_NOTIFICACIONES);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping(value = "/enviarNotificacion", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Envia una notificacion desde el front de la aplicación")
    public ResponseEntity<BodyEncriptado> enviarEmail(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "documentos", required = false) MultipartFile[] multipartFiles,
            @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();

        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<NotificacionDTO> df = this.notificacionService.enviarNotificacion(
                httpServletRequest, bodyEncriptado, multipartFiles
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, EtiquetaNemonico.ACCION_ENVIO_DE_EMAIL_NOTIFICACION
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados a la ficha y carpeta")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.notificacionService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_DOCUMENTO_OBTENER);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
