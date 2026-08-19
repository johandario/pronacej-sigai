package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AreasSituacionEducativaLaboralOcioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionEducativaLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionEducativaLaboralOcioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.SituacionEducativaLaboralService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/situacion-educativa-laboral")
@SecurityRequirement(name = "Authorization")
public class SituacionEducativaLaboralController {
    
    private SituacionEducativaLaboralService situacionEducativaLaboralService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerAreasSituacionEducativaLaboralOcio")
    @Operation(summary = "Obtiene las áreas de la situación educativa/laborale/ocio válida")
    public ResponseEntity<BodyEncriptado> obtenerSituacionesEducativasLaboralesOcio(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<AreasSituacionEducativaLaboralOcioDTO> df = this.situacionEducativaLaboralService.obtenerAreasSituacionEducativaLaboralOcio(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SITUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerSituacionesEducativasLaboralesOcioPaginado")
    @Operation(summary = "Obtiene las situaciones educativas/laborales/ocio válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerSituacionesEducativasLaboralesOcioPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionEducativaLaboralOcioDTO>> df = this.situacionEducativaLaboralService.obtenerSituacionesEducativasLaboralesOcio(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SITUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerLaboralesPaginado")
    @Operation(summary = "Obtiene los situaciones laborales válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerLaboralesPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<LaboralDTO>> df = this.situacionEducativaLaboralService.obtenerLaborales(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SITUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarSituacionEducativaLaboralOcio")
    @Operation(summary = "Elimina una situación educativa/laboral/ocio")
    public ResponseEntity<BodyEncriptado> eliminarSituacionEducativaLaboralOcio(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.situacionEducativaLaboralService.eliminarSituacionEducativaLaboralOcio(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_SITUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarLaboral")
    @Operation(summary = "Elimina un laboral")
    public ResponseEntity<BodyEncriptado> eliminarLaboral(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.situacionEducativaLaboralService.eliminarLaboral(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_SITUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearSituacionEducativaLaboral")
    @Operation(summary = "Crea o edita una situación educativa/laboral")
    public ResponseEntity<BodyEncriptado> crearSituacionEducativaLaboral(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        // Deserializar el DTO para determinar si es creación o edición
        SituacionEducativaLaboralDTO situacionEducativaLaboralDTO = new Gson().fromJson(body, SituacionEducativaLaboralDTO.class);

        RespuestaPorDefectoAuditoria<SituacionEducativaLaboralDTO> df = this.situacionEducativaLaboralService.crearSituacionEducativaLaboral(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si hay elementos que no son nuevos (token != "0")
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si hay elementos en las listas que no sean nuevos (token != "0")
        if (situacionEducativaLaboralDTO.getListaSituEducLaboOcio() != null) {
            for (SituacionEducativaLaboralOcioDTO item : situacionEducativaLaboralDTO.getListaSituEducLaboOcio()) {
                if (item.getTokenIdentificador() != null && !item.getTokenIdentificador().equals("0")) {
                    esEdicion = true;
                    break;
                }
            }
        }

        if (!esEdicion && situacionEducativaLaboralDTO.getListaLaboral() != null) {
            for (LaboralDTO item : situacionEducativaLaboralDTO.getListaLaboral()) {
                if (item.getTokenIdentificador() != null && !item.getTokenIdentificador().equals("0")) {
                    esEdicion = true;
                    break;
                }
            }
        }

        if (esEdicion) {
            // Si hay elementos existentes, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_SITUACION_EDUCATIVA_LABORAL;
        } else {
            // Si todos son nuevos o no hay elementos, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_SITUACION_EDUCATIVA_LABORAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
}