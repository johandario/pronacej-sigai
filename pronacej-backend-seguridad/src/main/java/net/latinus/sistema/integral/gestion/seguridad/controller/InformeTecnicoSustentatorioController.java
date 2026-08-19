package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeTecnicoSustentatorioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.InformeTecnicoSustentatorioService;
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
@RequestMapping(path = "api/v1/informe-tecnico")
@SecurityRequirement(name = "Authorization")
public class InformeTecnicoSustentatorioController {

    private InformeTecnicoSustentatorioService informeTecnicoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerInformesTecnicosPaginado")
    @Operation(summary = "Obtiene los informes técnicos válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerInformesTecnicosPaginado(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeTecnicoSustentatorioDTO>> df =
                this.informeTecnicoService.obtenerInformesTecnicosPaginado(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_INFORME_TECNICO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarInformeTecnico")
    @Operation(summary = "Elimina un informe técnico")
    public ResponseEntity<BodyEncriptado> eliminarInformeTecnico(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df =
                this.informeTecnicoService.eliminarInformeTecnico(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_INFORME_TECNICO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearInformeTecnico")
    @Operation(summary = "Crea o edita un informe técnico")
    public ResponseEntity<BodyEncriptado> crearInformeTecnico(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        // Deserializar el DTO para determinar si es creación o edición
        InformeTecnicoSustentatorioDTO informeTecnicoDTO = new Gson().fromJson(body, InformeTecnicoSustentatorioDTO.class);

        RespuestaPorDefectoAuditoria<InformeTecnicoSustentatorioDTO> df =
                this.informeTecnicoService.crearInformeTecnico(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición
        String accionAuditoria;
        if (informeTecnicoDTO.getEsEdicion() != null && informeTecnicoDTO.getEsEdicion()) {
            // Si es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_INFORME_TECNICO;
        } else {
            // Si es creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_INFORME_TECNICO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}