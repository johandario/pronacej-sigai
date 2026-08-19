package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ApreciacionFinalTratamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ApreciacionFinalTratamientoService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/apreciacion-final-tratamiento")
@SecurityRequirement(name = "Authorization")
public class ApreciacionFinalTratamientoController {
    
    private ApreciacionFinalTratamientoService apreciacionFinalService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerApreciacionesFinalesPaginado")
    @Operation(summary = "Obtiene las apreciaciones finales válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerApreciacionesFinalesPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<ApreciacionFinalTratamientoDTO>> df = 
            this.apreciacionFinalService.obtenerApreciacionesFinalesPaginado(
                httpServletRequest, 
                bodyEncriptado, 
                EtiquetaNemonico.NEMONICO_MENU_APRECIACION_FINAL);
            
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_APRECIACION_FINAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarApreciacionFinal")
    @Operation(summary = "Elimina una apreciación final")
    public ResponseEntity<BodyEncriptado> eliminarApreciacionFinal(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
            this.apreciacionFinalService.eliminarApreciacionFinal(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_APRECIACION_FINAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarSituacion")
    @Operation(summary = "Elimina una situación específica")
    public ResponseEntity<BodyEncriptado> eliminarSituacion(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
            this.apreciacionFinalService.eliminarSituacion(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_SITUACION_ADOLESCENTE);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarFactor")
    @Operation(summary = "Elimina un factor específico")
    public ResponseEntity<BodyEncriptado> eliminarFactor(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
            this.apreciacionFinalService.eliminarFactor(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_FACTOR_PRESENTE);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearApreciacionFinal")
    @Operation(summary = "Crea o actualiza una apreciación final")
    public ResponseEntity<BodyEncriptado> crearApreciacionFinal(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        ApreciacionFinalTratamientoDTO apreciacionFinalDTO = new Gson().fromJson(bodyDesencriptado, ApreciacionFinalTratamientoDTO.class);

        RespuestaPorDefectoAuditoria<ApreciacionFinalTratamientoDTO> df = 
            this.apreciacionFinalService.crearApreciacionFinal(
                httpServletRequest, 
                bodyEncriptado, 
                EtiquetaNemonico.NEMONICO_MENU_APRECIACION_FINAL);

        // Determinar la acción de auditoría basada en si es edición
        String accionAuditoria;
        if (apreciacionFinalDTO.getEsEdicion() != null && apreciacionFinalDTO.getEsEdicion()) {
            // Si es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_APRECIACION_FINAL;
        } else {
            // Si es creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_APRECIACION_FINAL;
        }
                
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}