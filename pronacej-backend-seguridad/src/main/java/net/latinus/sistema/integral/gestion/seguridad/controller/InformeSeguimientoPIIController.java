package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeSeguimientoPIIDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.InformeSeguimientoPIIService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/informe-seguimiento")
@SecurityRequirement(name = "Authorization")
public class InformeSeguimientoPIIController {
    
    private InformeSeguimientoPIIService informeSeguimientoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerInformesSeguimientoPaginado")
    @Operation(summary = "Obtiene los informes de seguimiento válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerInformesSeguimientoPaginado(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeSeguimientoPIIDTO>> df = 
                this.informeSeguimientoService.obtenerInformesSeguimientoPaginado(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                 bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_INFORME_SEGUIMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarInformeSeguimiento")
    @Operation(summary = "Elimina un informe de seguimiento")
    public ResponseEntity<BodyEncriptado> eliminarInformeSeguimiento(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
                this.informeSeguimientoService.eliminarInformeSeguimiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_INFORME_SEGUIMIENTO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearInformeSeguimiento")
    @Operation(summary = "Crea o edita un informe de seguimiento")
    public ResponseEntity<BodyEncriptado> crearInformeSeguimiento(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        // Deserializar el DTO para determinar si es creación o edición
        InformeSeguimientoPIIDTO informeSeguimientoDTO = new Gson().fromJson(body, InformeSeguimientoPIIDTO.class);

        RespuestaPorDefectoAuditoria<InformeSeguimientoPIIDTO> df = 
                this.informeSeguimientoService.crearInformeSeguimiento(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición
        String accionAuditoria;
        if (informeSeguimientoDTO.getEsEdicion() != null && informeSeguimientoDTO.getEsEdicion()) {
            // Si es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_INFORME_SEGUIMIENTO;
        } else {
            // Si es creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_INFORME_SEGUIMIENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}