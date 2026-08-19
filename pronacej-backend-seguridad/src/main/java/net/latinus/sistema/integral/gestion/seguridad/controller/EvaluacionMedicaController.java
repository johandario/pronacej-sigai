package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico.DiagnosticoService;
import net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico.EstadoNutricionalService;
import net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico.EvaluacionMedicaService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica.CriterioEvaluacionMedicaSeguimientoService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacionMedica")
@SecurityRequirement(name = "Authorization")
public class EvaluacionMedicaController {

    private EvaluacionMedicaService evaluacionMedicaService;
    private DiagnosticoService diagnosticoService;
    private EstadoNutricionalService estadoNutricionalService;
    private CriterioEvaluacionMedicaSeguimientoService criterioEvaluacionMedicaSeguimientoService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerEvaluacionMedicaPorFichaMedica")
    @Operation(summary = "Obtener evaluacion medica por token id de ficha medica")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionPorFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaDTO>> df = this.evaluacionMedicaService.getEvaluacionMedicaByIdFichaMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEvaluacionMedicaPorTokenId")
    @Operation(summary = "Obtener evaluacion medica por token identificador")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionPorTokenId(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO> df = this.evaluacionMedicaService.getEvaluacionMedicaByIdTokenId(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEvaluacionMedica")
    @Operation(summary = "Crear evaluación médica")
    public ResponseEntity<BodyEncriptado> crearEvaluacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO> df = this.evaluacionMedicaService.postEvaluacionMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarEvaluacionMedica")
    @Operation(summary = "Actualizar evaluación médica")
    public ResponseEntity<BodyEncriptado> actualizarEvaluacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaDTO> df = this.evaluacionMedicaService.updateEvaluacionMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarEvaluacionMedica")
    @Operation(summary = "Eliminar evaluación médica")
    public ResponseEntity<BodyEncriptado> eliminarEvaluacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionMedicaService.deleteEvaluacionMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDiagnosticoPorEvaluacionMedica")
    @Operation(summary = "Obtener diagnostico por token id de evaluacion medica")
    public ResponseEntity<BodyEncriptado> obtenerDiagnosticoPorEvaluacionMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DiagnosticoDTO>> df = this.diagnosticoService.getDiagnosticoByIdEvaluacionMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearDiagnostico")
    @Operation(summary = "Crear diagnostico médico")
    public ResponseEntity<BodyEncriptado> crearDiagnostico(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<DiagnosticoDTO> df = this.diagnosticoService.postDiagnostico(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarDiagnostico")
    @Operation(summary = "Actualizar diagnostico médico")
    public ResponseEntity<BodyEncriptado> actualizarDiagnostico(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<DiagnosticoDTO> df = this.diagnosticoService.updateDiagnostico(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarDiagnostico")
    @Operation(summary = "Eliminar diagnostico médico")
    public ResponseEntity<BodyEncriptado> eliminarDiagnostico(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Boolean> df = this.diagnosticoService.deleteDiagnostico(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEstadoNutricionalPorEvaluacionMedica")
    @Operation(summary = "Obtener estado nutricional por token id de evaluacion medica")
    public ResponseEntity<BodyEncriptado> obtenerEstadoNutricionalPorEvaluacionMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EstadoNutricionalDTO>> df = this.estadoNutricionalService.getEstadoNutricionalByIdEvaluacionMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEstadoNuticional")
    @Operation(summary = "Crear Estado Nutricional")
    public ResponseEntity<BodyEncriptado> crearEstadoNutricional(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> df = this.estadoNutricionalService.postEstadoNutricional(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarEstadoNuticional")
    @Operation(summary = "Actualizar Estado Nutricional")
    public ResponseEntity<BodyEncriptado> actualizarEstadoNutricional(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> df = this.estadoNutricionalService.updateEstadoNutricional(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarEstadoNuticional")
    @Operation(summary = "Eliminar Estado Nutricional")
    public ResponseEntity<BodyEncriptado> eliminarEstadoNutricional(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Boolean> df = this.estadoNutricionalService.deleteEstadoNutricional(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCriteriosEvaluacionRelacionados")
    @Operation(summary = "Obtiene los criterios de evaluacion de seguimientos relacionadas a la evaluacion.")
    public ResponseEntity<BodyEncriptado> obtenerCriteriosEvaluacionRelacionados(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>> df = this.criterioEvaluacionMedicaSeguimientoService.
                getCriteriosDeEvaluacion(httpServletRequest, bodyEncriptado);
//        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
//                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ENFERMEDADES_RELACIONADAS
//        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
