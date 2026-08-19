package net.latinus.sistema.integral.gestion.seguridad.controller.ia;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ActaExternamientoService;
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
@RequestMapping(path = "api/v1/actaExternamiento")
@SecurityRequirement(name = "Authorization")
public class ActaExternamientoController {

    private ActaExternamientoService actaExternamientoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerActasExternamiento")
    @Operation(summary = "Obtiene el listado de actas de externamiento paginado")
    public ResponseEntity<BodyEncriptado> obtenerActasExternamiento(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<ActaExternamientoDTO>> df = this.actaExternamientoService.obtenerActasExternamiento(httpServletRequest,bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ACTAS_EXTERNAMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearActaExternamiento")
    @Operation(summary = "Crea un acta de externamiento")
    public ResponseEntity<BodyEncriptado> crearActaExternamiento(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.actaExternamientoService.crearActaExternamiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_ACTA_EXTERNAMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarActaExternamiento")
    @Operation(summary = "Edita un acta de externamiento")
    public ResponseEntity<BodyEncriptado> actualizarActaExternamiento(HttpServletRequest httpServletRequest,
                                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.actaExternamientoService.actualizarActaExternamiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ACTUALIZAR_ACTA_EXTERNAMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirActaFirmada")
    @Operation(summary = "Sube un acta firmada")
    public ResponseEntity<BodyEncriptado> subirActaFirmada(HttpServletRequest httpServletRequest,
                                                              @RequestParam("documento") MultipartFile multipartFile,
                                                           @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String body = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.actaExternamientoService.subirActaFirmada(httpServletRequest, multipartFile, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_SUBIR_ACTA_EXTERNAMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarActaExternamiento")
    @Operation(summary = "Elimina un acta de externamiento")
    public ResponseEntity<BodyEncriptado> eliminarActaExternamiento(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.actaExternamientoService.eliminarActaExternamiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_ACTA_EXTERNAMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }
}
