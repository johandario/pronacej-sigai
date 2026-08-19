package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeFinalAsistenciaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.InformeFinalAsistenciaService;
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
@RequestMapping(path = "api/v1/informe-final-asistencia")
@SecurityRequirement(name = "Authorization")
public class InformeFinalAsistenciaController {
    private InformeFinalAsistenciaService informeFinalAsistenciaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/lista")
    @Operation(summary = "Obtener lista de registros de informes finales de asistencia")
    public ResponseEntity<BodyEncriptado> obtenerPlanTratamiento(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAsistenciaDTO>> df = this.informeFinalAsistenciaService.obtenerInformes(httpServletRequest, bodyEncriptado);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Creación de plan")
    public ResponseEntity<BodyEncriptado> crearPlanTratamiento(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> df = this.informeFinalAsistenciaService.crearInforme(httpServletRequest, bodyEncriptado);
        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_INFORME_FINAL_ASISTENCIA;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_INFORME_FINAL_ASISTENCIA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminación de plan")
    public ResponseEntity<BodyEncriptado> eliminarPlanTratamiento(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> df = this.informeFinalAsistenciaService.eliminarInforme(httpServletRequest, bodyEncriptado);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_INFORME_FINAL_ASISTENCIA;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
