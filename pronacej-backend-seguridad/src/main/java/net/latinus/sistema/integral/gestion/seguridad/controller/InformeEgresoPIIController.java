package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeEgresoPIIDTO;
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
import net.latinus.sistema.integral.gestion.seguridad.service.IA.InformeEgresoPIIService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/informe-egreso-pii")
@SecurityRequirement(name = "Authorization")
public class InformeEgresoPIIController {
    
    private InformeEgresoPIIService informeEgresoPIIService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerInformesEgresoPaginado")
    @Operation(summary = "Obtiene los informes de egreso PII válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerInformesEgresoPaginado(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeEgresoPIIDTO>> df = 
            this.informeEgresoPIIService.obtenerInformesEgreso(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_INFORME_EGRESO_PII);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarInformeEgreso")
    @Operation(summary = "Elimina un informe de egreso PII")
    public ResponseEntity<BodyEncriptado> eliminarInformeEgreso(HttpServletRequest httpServletRequest,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
            this.informeEgresoPIIService.eliminarInformeEgreso(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_INFORME_EGRESO_PII);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearInformeEgreso")
    @Operation(summary = "Crea o edita un informe de egreso PII")
    public ResponseEntity<BodyEncriptado> crearInformeEgreso(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        // Deserializar el DTO para determinar si es creación o edición
        InformeEgresoPIIDTO informeEgresoDTO = new Gson().fromJson(body, InformeEgresoPIIDTO.class);

        RespuestaPorDefectoAuditoria<InformeEgresoPIIDTO> df = 
            this.informeEgresoPIIService.crearInformeEgreso(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición
        String accionAuditoria;
        if (informeEgresoDTO.getEsEdicion() != null && informeEgresoDTO.getEsEdicion()) {
            // Si es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_INFORME_EGRESO_PII;
        } else {
            // Si es creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_INFORME_EGRESO_PII;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}