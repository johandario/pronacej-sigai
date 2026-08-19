package net.latinus.sistema.integral.gestion.seguridad.controller.salida;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.ReforzamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RelacionEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.SesionReforzamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.salida.RelacionEgresoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/relacionEgreso")
@SecurityRequirement(name = "Authorization")
public class RelacionEgresoController {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private RelacionEgresoService relacionEgresoService;

    @PostMapping("/obtenerAdolescentes")
    @Operation(summary = "Obtiene el listado de adolescentes")
    public ResponseEntity<BodyEncriptado> obtenerAdolescentes(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<RelacionEgresoDTO>> df = this.relacionEgresoService.obtenerAdolescentes(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerReforzamientos")
    @Operation(summary = "Obtiene el listado de reforzamientos")
    public ResponseEntity<BodyEncriptado> obtenerReforzamientos(HttpServletRequest httpServletRequest,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<ReforzamientoDTO>> df = this.relacionEgresoService.obtenerReforzamientos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_REFORZAMIENTOS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerReforzamientoPorToken")
    @Operation(summary = "Obtiene el reforzamiento especificado")
    public ResponseEntity<BodyEncriptado> obtenerReforzamientoPorToken(HttpServletRequest httpServletRequest,
                                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<ReforzamientoDTO> df = this.relacionEgresoService.obtenerReforzamientoPorToken(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_REFORZAMIENTOS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearReforzamiento")
    @Operation(summary = "Crea un reforzamiento")
    public ResponseEntity<BodyEncriptado> crearReforzamiento(HttpServletRequest httpServletRequest,
                                                             @RequestParam(value = "documentos", required = false) MultipartFile[] multipartFiles,
                                                             @RequestParam(value = "constancias", required = false) MultipartFile[] constancias,
                                                             @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaInicio = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        ReforzamientoDTO reforzamientoDTO = new Gson().fromJson(body, ReforzamientoDTO.class);

        RespuestaPorDefectoAuditoria<Boolean> df = this.relacionEgresoService.crearReforzamiento(httpServletRequest, multipartFiles, constancias, bodyEncriptado);

        // Determinar la acción de auditoría basada en si el reforzamiento ya existe
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si el reforzamiento ya existe (tiene tokenIdentificador)
        if (reforzamientoDTO.getTokenIdentificador() != null && !reforzamientoDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        // Si no está claro por el token principal, verificar sesiones existentes
        if (!esEdicion && reforzamientoDTO.getSesiones() != null) {
            for (SesionReforzamientoDTO sesion : reforzamientoDTO.getSesiones()) {
                if (sesion.getTokenIdentificador() != null && !sesion.getTokenIdentificador().equals("0")) {
                    esEdicion = true;
                    break;
                }
            }
        }

        if (esEdicion) {
            // Si hay elementos existentes, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_REFORZAMIENTO;
        } else {
            // Si todos son nuevos o no hay elementos, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_REFORZAMIENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                body, df, fechaInicio, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarReforzamiento")
    @Operation(summary = "Actualiza el reforzamiento")
    public ResponseEntity<BodyEncriptado> actualizarReforzamiento(HttpServletRequest httpServletRequest,
                                                                  @RequestParam(value = "constancias", required = false) MultipartFile[] constancias,
                                                                  @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaInicio = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si hay nuevas sesiones o solo actualización
        ReforzamientoDTO reforzamientoDTO = new Gson().fromJson(body, ReforzamientoDTO.class);

        RespuestaPorDefectoAuditoria<Boolean> df = this.relacionEgresoService.actualizarReforzamiento(httpServletRequest, constancias, bodyEncriptado);

        // Determinar la acción de auditoría basada en si hay nuevas sesiones
        String accionAuditoria;
        boolean tieneNuevasSesiones = false;

        // Verificar si hay sesiones nuevas (sin tokenIdentificador o con "0")
        if (reforzamientoDTO.getSesiones() != null) {
            for (SesionReforzamientoDTO sesion : reforzamientoDTO.getSesiones()) {
                if (sesion.getTokenIdentificador() == null || sesion.getTokenIdentificador().equals("0")) {
                    tieneNuevasSesiones = true;
                    break;
                }
            }
        }

        if (tieneNuevasSesiones) {
            // Si hay nuevas sesiones, considerarlo como creación de contenido adicional
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_REFORZAMIENTO;
        } else {
            // Si solo actualiza información existente, es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_REFORZAMIENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                body, df, fechaInicio, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/removerReforzamiento")
    @Operation(summary = "Remueve el reforzamiento")
    public ResponseEntity<BodyEncriptado> removerReforzamiento(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.relacionEgresoService.removerReforzamiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_REMOVER_REFORZAMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados al reforzamiento y sus sesiones")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.relacionEgresoService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_REFORZAMIENTO_DOCUMENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}