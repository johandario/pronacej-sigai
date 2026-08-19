package net.latinus.sistema.integral.gestion.seguridad.controller.ia;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.SeguimientoConductualDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.SeguimientoPsicologicoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.SeguimientoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/seguimiento")
@SecurityRequirement(name = "Authorization")
public class SeguimientoController {

    private SeguimientoService seguimientoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    //region Seguimiento Psicologico

    @PostMapping("/obtenerSeguimientosPsicologicos")
    @Operation(summary = "Obtiene el listado de seguimientos psicologicos paginado")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientosPsicologicos(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoPsicologicoDTO>> df = this.seguimientoService.obtenerSeguimientosPsicologicos(httpServletRequest,bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SEGUIMIENTOS_PSICOLOGICOS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearSeguimientoPsicologico")
    @Operation(summary = "Crea o edita un seguimiento psicologico")
    public ResponseEntity<BodyEncriptado> crearSeguimientoPsicologico(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        SeguimientoPsicologicoDTO seguimientoPsicologicoDTO = new Gson().fromJson(body, SeguimientoPsicologicoDTO.class);

        RespuestaPorDefectoAuditoria<Boolean> df = this.seguimientoService.crearSeguimientoPsicologico(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si el ID existe (edición) o no (creación)
        String accionAuditoria;
        if (seguimientoPsicologicoDTO.getIdSeguimientoPsicologico() != null && seguimientoPsicologicoDTO.getIdSeguimientoPsicologico() > 0) {
            // Si hay ID, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_SEGUIMIENTO_PSICOLOGICO;
        } else {
            // Si no hay ID, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_SEGUIMIENTO_PSICOLOGICO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarSeguimientoPsicologico")
    @Operation(summary = "Edita un seguimiento psicologico")
    public ResponseEntity<BodyEncriptado> actualizarSeguimientoPsicologico(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.seguimientoService.actualizarSeguimientoPsicologico(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_SEGUIMIENTO_PSICOLOGICO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarSeguimientoPsicologico")
    @Operation(summary = "Elimina un seguimiento psicologico")
    public ResponseEntity<BodyEncriptado> eliminarSeguimientoPsicologico(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.seguimientoService.eliminarSeguimientoPsicologico(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_SEGUIMIENTO_PSICOLOGICO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    //endregion

    //region Seguimiento Conductual

    @PostMapping("/obtenerSeguimientosConductuales")
    @Operation(summary = "Obtiene el listado de seguimientos conductuales paginado")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientosConductuales(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoConductualDTO>> df = this.seguimientoService.obtenerSeguimientosConductuales(httpServletRequest,bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SEGUIMIENTOS_CONDUCTUALES);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearSeguimientoConductual")
    @Operation(summary = "Crea o edita un seguimiento conductual")
    public ResponseEntity<BodyEncriptado> crearSeguimientoConductual(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        SeguimientoConductualDTO seguimientoConductualDTO = new Gson().fromJson(body, SeguimientoConductualDTO.class);

        RespuestaPorDefectoAuditoria<Boolean> df = this.seguimientoService.crearSeguimientoConductual(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si el ID existe (edición) o no (creación)
        String accionAuditoria;
        if (seguimientoConductualDTO.getIdSeguimientoConductual() != null && seguimientoConductualDTO.getIdSeguimientoConductual() > 0) {
            // Si hay ID, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_SEGUIMIENTO_CONDUCTUAL;
        } else {
            // Si no hay ID, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_SEGUIMIENTO_CONDUCTUAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarSeguimientoConductual")
    @Operation(summary = "Edita un seguimiento conductual")
    public ResponseEntity<BodyEncriptado> actualizarSeguimientoConductual(HttpServletRequest httpServletRequest,
                                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.seguimientoService.actualizarSeguimientoConductual(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_SEGUIMIENTO_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarSeguimientoConductual")
    @Operation(summary = "Elimina un seguimiento conductual")
    public ResponseEntity<BodyEncriptado> eliminarSeguimientoConductual(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.seguimientoService.eliminarSeguimientoConductual(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_SEGUIMIENTO_CONDUCTUAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    //endregion
}