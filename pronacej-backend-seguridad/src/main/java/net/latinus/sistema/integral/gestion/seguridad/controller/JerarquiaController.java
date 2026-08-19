package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaCentroEstadisticaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JerarquiaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/jerarquia")
@SecurityRequirement(name = "Authorization")
public class JerarquiaController {

    private JerarquiaService jerarquiaService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @GetMapping("/obtenerJerarquias")
    @Operation(summary = "Obtiene las jerarquia del sistema")
    public ResponseEntity<BodyEncriptado> obtenerJerarquias(HttpServletRequest httpServletRequest) throws Exception {
        Date fechaInicio = new Date();
        
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = this.jerarquiaService.obtenerJerarquias(httpServletRequest);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "", df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerJerarquiasPorNemonicoPadre")
    @Operation(summary = "Obtiene las jerarquia del sistema por nemónico padre")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiasPorNemonicoPadre(HttpServletRequest httpServletRequest,
                                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerJerarquiasPorNemonicoPadreLista")
    @Operation(summary = "Obtiene las jerarquías del sistema por una lista de nemónicos padre")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiasPorNemonicoPadreLista(HttpServletRequest httpServletRequest,
                                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Map<String, List<JerarquiaDTO>>> df =
                this.jerarquiaService.obtenerJerarquiasPorNemonicoPadreLista(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerJerarquiasPorNemonicoPadreCompleto")
    @Operation(summary = "Obtiene las jerarquías del sistema por nemónico padre con estructura jerárquica completa (hijos anidados recursivamente)")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiasPorNemonicoPadreCompleto(HttpServletRequest httpServletRequest,
                                                                                     @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = this.jerarquiaService.obtenerJerarquiasPorNemonicoPadreCompleto(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @GetMapping("/obtenerJerarquiaPorNumeroDeDocumento")
    @Operation(summary = "Obten una jerarquía válida por el número del documento")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiaPorNumeroDeDocumento(
            HttpServletRequest httpServletRequest) throws Exception {
        Date fechaInicio = new Date();
        
        RespuestaPorDefectoAuditoria<JerarquiaDTO> df = this.jerarquiaService.obtenerJerarquiaPorNumeroDeDocumento(httpServletRequest);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "", df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearOEditarJerarquia")
    @Operation(summary = "Crea o edita una jerarquia del sistema")
    public ResponseEntity<BodyEncriptado> crearOEditarJerarquia(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        // Deserializar el DTO para determinar si es creación o edición
        JerarquiaDTO jerarquiaDTO = new Gson().fromJson(body, JerarquiaDTO.class);

        // Determinar si es creación o edición
        boolean esEdicion = jerarquiaDTO.getId() != null && jerarquiaDTO.getId() != 0;

        RespuestaPorDefectoAuditoria<JerarquiaDTO> df;
        String accionAuditoria;

        if (esEdicion) {
            // Es una edición
            df = this.jerarquiaService.actualizarJerarquia(httpServletRequest, jerarquiaDTO);
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_JERARQUIA;
        } else {
            // Es una creación
            df = this.jerarquiaService.crearJerarquia(httpServletRequest, jerarquiaDTO);
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_JERARQUIA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearJerarquia")
    @Operation(summary = "Crea una jerarquia para el sistema")
    public ResponseEntity<BodyEncriptado> crearJerarquia(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<JerarquiaDTO> df = this.jerarquiaService.crearJerarquia(httpServletRequest, new Gson().fromJson(body, JerarquiaDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_JERARQUIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarJerarquia")
    @Operation(summary = "Actualiza una jerarquia del sistema")
    public ResponseEntity<BodyEncriptado> actualizarJerarquia(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<JerarquiaDTO> df = this.jerarquiaService.actualizarJerarquia(httpServletRequest, new Gson().fromJson(body, JerarquiaDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_JERARQUIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/removerJerarquia")
    @Operation(summary = "Remueve una jerarquia del sistema")
    public ResponseEntity<BodyEncriptado> removerJerarquia(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<JerarquiaDTO> df = this.jerarquiaService.removerJerarquia(httpServletRequest, new Gson().fromJson(body, JerarquiaDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_JERARQUIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerJerarquiasPorJerarquiaPadreFuncionario")
    @Operation(summary = "Obtiene las jerarquías hijas, filtradas por la jerarquía padre asociada al funcionario actual")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiasPorJerarquiaPadreFuncionario(HttpServletRequest httpServletRequest) throws Exception {
        Date fechaInicio = new Date();
        
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df =
                this.jerarquiaService.obtenerJerarquiasPorJerarquiaPadreFuncionario(httpServletRequest);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "", df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerJerarquiasPorTokenPadre")
    @Operation(summary = "Obtiene las jerarquia del sistema por token padre")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiasPorTokenPadre(HttpServletRequest httpServletRequest,
                                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = this.jerarquiaService.obtenerJerarquiasPorTokenPadre(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_JERARQUIA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEstadisticasFichasPorCentro")
    @Operation(summary = "Obtiene estadísticas de fichas por centro")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticasFichasPorCentro(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception{

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<List<FichaCentroEstadisticaDTO>> respuesta =
                jerarquiaService.obtenerEstadisticasFichasPorCentro(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_REPORTE_POBLACION_CENTROS
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}