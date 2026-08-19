package net.latinus.sistema.integral.gestion.seguridad.controller;


import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.ConsultaAtencionIntegralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.FichaMedicaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.AntecedenteFamiliarDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.IngresoCentroJuvenilDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaMedicaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico.ConsultaAtencionIntegralService;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica.*;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacionMedica")
@SecurityRequirement(name = "Authorization")
public class FichaMedicaController {

    private FichaMedicaService fichaMedicaService;
    private AntecedenteFamiliarService antecedenteFamiliarService;
    private IngresoCentroJuvenilService ingresoCentroJuvenilService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private FichaMedicaEnfermedadServiceImpl fichaMedicaEnfermedadService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ConsultaAtencionIntegralService consultaAtencionIntegralService;
    private FichaMedicaDocumentoService fichaMedicaDocumentoService;

    @PostMapping("/obtenerFichasMedicas")
    @Operation(summary = "Obtener fichas médicas del sistema")
    public ResponseEntity<BodyEncriptado> obtenerFichasMedicas(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaDTO>> df = this.fichaMedicaService.getFichaMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(
                    this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerFichaMedicaPorFichaIdentificacion")
    @Operation(summary = "Obtener ficha médica por token id de ficha identificación")
    public ResponseEntity<BodyEncriptado> obtenerFichaPorFichaIdentificacion(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<FichaMedicaDTO> df = this.fichaMedicaService.getFichaMedicaByIdFichaIdentificacion(httpServletRequest, bodyEncriptado);

        Date inicioRequest = new Date();

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), df, inicioRequest,
                EtiquetaNemonico.ACCION_OBTENER_FICHA_MEDICA);

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearFichaMedica")
    @Operation(summary = "Crea una ficha médica en el sistema")
    public ResponseEntity<BodyEncriptado> crearFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception{
        RespuestaPorDefectoAuditoria<FichaMedicaDTO> df = this.fichaMedicaService.postFichaMedica(httpServletRequest, bodyEncriptado);

        Date inicioRequest = new Date();

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), df, inicioRequest,
                EtiquetaNemonico.ACCION_CREAR_FICHA_MEDICA);

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarFichaMedica")
    @Operation(summary = "Actualiza la ficha médica en el sistema")
    public ResponseEntity<BodyEncriptado> actualizarFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception{
        RespuestaPorDefectoAuditoria<FichaMedicaDTO> df = this.fichaMedicaService.updateFichaMedica(httpServletRequest, bodyEncriptado);

        Date inicioRequest = new Date();

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), df, inicioRequest,
                EtiquetaNemonico.ACCION_ACTUALIZAR_FICHA_MEDICA);

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarFichaMedica")
    @Operation(summary = "Elimina la ficha médica del sistema")
    public ResponseEntity<BodyEncriptado> eliminarFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception{
        RespuestaPorDefectoAuditoria<Boolean> df = this.fichaMedicaService.deleteFichaMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCentrosJuvenilesPorFichaMedica")
    @Operation(summary = "Obtiene los ingresos a centros juveniles asociados a la ficha médica")
    public ResponseEntity<BodyEncriptado> obtenerIngresoCentrosPorFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<IngresoCentroJuvenilDTO>> df = this.ingresoCentroJuvenilService.getCentrosJuvenilesByTokenIdFichaMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearCentroJuvenil")
    @Operation(summary = "Crea un ingreso a centro juvenil asociado a la ficha médica")
    public ResponseEntity<BodyEncriptado> crearIngresoCentro(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> df = this.ingresoCentroJuvenilService.postIngresoCentroJuvenil(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarCentroJuvenil")
    @Operation(summary = "Actualiza un ingreso a centro juvenil asociado a la ficha médica")
    public ResponseEntity<BodyEncriptado> actualizarIngresoCentro(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> df = this.ingresoCentroJuvenilService.updateIngresoCentroJuvenil(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarCentroJuvenil")
    @Operation(summary = "Elimina un ingreso a centro juvenil asociado a la ficha médica")
    public ResponseEntity<BodyEncriptado> eliminarIngresoCentro(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Boolean> df = this.ingresoCentroJuvenilService.deleteIngresoCentroJuvenil(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerAntecedentesFamiliaresPorFichaMedica")
    @Operation(summary = "Obtiene los antecedentes familiares asociados a la ficha médica")
    public ResponseEntity<BodyEncriptado> obtenerAntecedentesFamiliaresPorFichaMedica(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<AntecedenteFamiliarDTO>> df = this.antecedenteFamiliarService.getAntecedenteFamiliarByTokenIdFichaMedica(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearAntecedenteFamiliar")
    @Operation(summary = "Crea un antecedente familiar asociado a la ficha médica")
    public ResponseEntity<BodyEncriptado> crearAntecedenteFamiliar(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> df = this.antecedenteFamiliarService.postAntecedenteFamiliar(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarAntecedenteFamiliar")
    @Operation(summary = "Actualiza un antecedente familiar asociado a la ficha médica")
    public ResponseEntity<BodyEncriptado> actualizarAntecedenteFamiliar(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> df = this.antecedenteFamiliarService.updateAntecedenteFamiliar(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarAntecedenteFamiliar")
    @Operation(summary = "Elimina un antecedente familiar asociado a la ficha médica")
    public ResponseEntity<BodyEncriptado> eliminarAntecedenteFamiliar(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Boolean> df = this.antecedenteFamiliarService.deleteAntecedenteFamiliar(httpServletRequest, bodyEncriptado);
        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEnfermedadesRelacionadas")
    @Operation(summary = "Obtiene la informacion de enfermedades de las personas relacionadas a una ficha medica.")
    public ResponseEntity<BodyEncriptado> obtenerPersonaRelacionadaEnfermedad(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaEnfermedadDTO>> df = this.fichaMedicaEnfermedadService.
                getFichaMedicaEnfermedades(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ENFERMEDADES_RELACIONADAS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearConsultaAtencion")
    @Operation(summary = "Crear o editar una consulta de atención integral")
    public ResponseEntity<BodyEncriptado> crearConsulta(HttpServletRequest request,
                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date inicioRequest = new Date();
        RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> respuesta = consultaAtencionIntegralService.crearConsulta(request, bodyEncriptado);

        String accion = respuesta.getData() != null && Boolean.TRUE.equals(respuesta.getData().getEsEdicion())
                ? "Editar Consulta de Atención Integral"
                : "Crear Consulta de Atención Integral";

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                request, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), respuesta, inicioRequest, accion);

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/listarConsultasAtencionPorFicha")
    @Operation(summary = "Listar consultas de atención integral por ficha médica")
    public ResponseEntity<BodyEncriptado> getConsultaAtencionByIdFichaMedica(HttpServletRequest httpServletRequest,
                                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date inicioRequest = new Date();
        RespuestaPorDefectoAuditoria<PaginacionResponse<ConsultaAtencionIntegralDTO>> respuesta =
                consultaAtencionIntegralService.getConsultaAtencionByIdFichaMedica(httpServletRequest, bodyEncriptado);

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), respuesta, inicioRequest,
                "Listar Consultas de Atención Integral por Ficha Médica");

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscarConsultaAtencionPorToken")
    @Operation(summary = "Obtener una consulta de atención integral por token")
    public ResponseEntity<BodyEncriptado> getConsultaActividadIntegralByIdTokenId(HttpServletRequest httpServletRequest,
                                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date inicioRequest = new Date();
        RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> respuesta =
                consultaAtencionIntegralService.getConsultaActividadIntegralByIdTokenId(httpServletRequest, bodyEncriptado);

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), respuesta, inicioRequest,
                "Buscar Consulta de Atención Integral por Token");

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarConsultaAtencion")
    @Operation(summary = "Eliminar consulta de atención integral")
    public ResponseEntity<BodyEncriptado> deleteConsultaActividadIntegral(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date inicioRequest = new Date();
        RespuestaPorDefectoAuditoria<Boolean> respuesta =
                consultaAtencionIntegralService.deleteConsultaActividadIntegral(httpServletRequest, bodyEncriptado);

        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(), respuesta, inicioRequest,
                "Eliminar Consulta de Atención Integral");

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de ficha médica")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.fichaMedicaDocumentoService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData(),
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_MEDICA_SUBIDA_DE_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obten todos los documentos asociados al registro de pertenencias")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.fichaMedicaDocumentoService.obtenerDocumentos(
                httpServletRequest, new Gson().fromJson(bodyDesencriptado, FichaMedicaDocumentosRequest.class)
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_MEDICA_OBTENCION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumento")
    @Operation(summary = "Eliminar documentos asociados a detalle")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        FichaMedicaDocumentoDTO fichaMedicaDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaMedicaDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<FichaMedicaDocumentoDTO> df = this.fichaMedicaDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, fichaMedicaDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_MEDICA_ELIMINACION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
