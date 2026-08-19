package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaProgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionMedicaProgresoDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.EvaluacionMedicaProgresoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico.EvaluacionMedicaProgresoService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.EvaluacionMedicaProgresoDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacionMedica")
@SecurityRequirement(name = "Authorization")
public class EvaluacionMedicaProgresoController {

    private EvaluacionMedicaProgresoService evaluacionMedicaProgresoService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private EvaluacionMedicaProgresoDocumentoService evaluacionMedicaProgresoDocumentoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerEvaluacionMedicaProgresoPorFichaMedica")
    @Operation(summary = "Obtener evaluacion medica por token id de ficha medica")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionPorFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaProgresoDTO>> df = this.evaluacionMedicaProgresoService.getEvaluacionMedicaProgresoByIdFichaMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEvaluacionMedicaProgreso")
    @Operation(summary = "Crear evaluación médica")
    public ResponseEntity<BodyEncriptado> crearEvaluacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> df = this.evaluacionMedicaProgresoService.postEvaluacionMedicaProgreso(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEvaluacionMedicaProgresoPorTokenId")
    @Operation(summary = "Obtener evaluacion medica por token identificador")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionPorTokenId(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> df = this.evaluacionMedicaProgresoService.getEvaluacionMedicaProgresoByIdTokenId(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarEvaluacionMedicaProgreso")
    @Operation(summary = "Eliminar evaluación médica")
    public ResponseEntity<BodyEncriptado> eliminarEvaluacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionMedicaProgresoService.deleteEvaluacionMedicaProgreso(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarEvaluacionMedicaProgreso")
    @Operation(summary = "Actualizar evaluación médica")
    public ResponseEntity<BodyEncriptado> actualizarEvaluacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> df = this.evaluacionMedicaProgresoService.updateEvaluacionMedicaProgreso(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirDocumentoEvaluacionMedicaProgreso")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de evaluación médica de progreso")
    public ResponseEntity<BodyEncriptado> subirDocumentoEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest,
                                                                                 @RequestParam("documento") MultipartFile multipartFile,
                                                                                 @RequestParam("body") String bodyEncriptadoString) throws Exception {
        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.evaluacionMedicaProgresoDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        // Registrar auditoría si es necesario
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, "ACCION_USUARIO_EVAL_MED_PROG_SUBIDA_DE_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentosEvaluacionMedicaProgreso")
    @Operation(summary = "Obtiene todos los documentos asociados al registro de evaluación médica de progreso")
    public ResponseEntity<BodyEncriptado> obtenerDocumentosEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest,
                                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        EvaluacionMedicaProgresoDocumentoRequest requestObj = new Gson().fromJson(bodyDesencriptado, EvaluacionMedicaProgresoDocumentoRequest.class);

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df =
                this.evaluacionMedicaProgresoDocumentoService.obtenerDocumentos(httpServletRequest, requestObj);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_EVAL_MED_PROG_OBTENCION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumentoEvaluacionMedicaProgreso")
    @Operation(summary = "Elimina la relación entre una evaluación médica de progreso y un documento")
    public ResponseEntity<BodyEncriptado> eliminarDocumentoEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest,
                                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        EvaluacionMedicaProgresoDocumentoDTO evalMedDocDTO = new Gson().fromJson(bodyDesencriptado, EvaluacionMedicaProgresoDocumentoDTO.class);

        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDocumentoDTO> df =
                this.evaluacionMedicaProgresoDocumentoService.eliminarRelacionConDocumento(httpServletRequest, evalMedDocDTO);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, "ACCION_USUARIO_EVAL_MED_PROG_ELIMINACION_DOCUMENTOS"
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }



}
