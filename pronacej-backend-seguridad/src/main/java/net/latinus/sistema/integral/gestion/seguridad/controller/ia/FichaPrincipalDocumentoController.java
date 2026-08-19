package net.latinus.sistema.integral.gestion.seguridad.controller.ia;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.DocumentoDTOFichaPrincipal;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaDeIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.FichaIdentificacionDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ficha-principal-documento")
@SecurityRequirement(name = "Authorization")
public class FichaPrincipalDocumentoController {

    private FichaIdentificacionDocumentoService fichaIdentificacionDocumentoService;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento y asocialo a la ficha principal del adolescente infractor")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        FichaPrincipalDocumentoDTO fichaPrincipalDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaPrincipalDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<DocumentoDTOFichaPrincipal> df = this.fichaIdentificacionDocumentoService.subirDocumento(
                httpServletRequest, fichaPrincipalDocumentoDTO, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_PRINCIPAL_SUBIDA_DE_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/editarDocumento")
    @Operation(summary = "Sube un documento y asocialo a la ficha principal del adolescente infractor")
    public ResponseEntity<BodyEncriptado> editarDocumento(HttpServletRequest httpServletRequest,
                                                          @RequestParam(value = "documento", required = false) MultipartFile multipartFile,
                                                          @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        FichaDeIdentificacionDocumentoDTO fichaDeIdentificacionDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaDeIdentificacionDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<FichaDeIdentificacionDocumentoDTO> df = this.fichaIdentificacionDocumentoService.editarDocumento(
                httpServletRequest, fichaDeIdentificacionDocumentoDTO, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_PRINCIPAL_SUBIDA_DE_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obten todos los documentos asocialo a la ficha principal (ficha de identificación) del adolescente infractor")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaDeIdentificacionDocumentoDTO>> df =
                this.fichaIdentificacionDocumentoService.obtenerDocumentosDeLaFichaDeIdentificacion(
                        httpServletRequest, new Gson().fromJson(bodyDesencriptado, FichaPrincipalDocumentosRequest.class)
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_PRINCIPAL_OBTENCION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }


    @PostMapping("/eliminar")
    @Operation(summary = "Obten todos los documentos asocialo a la ficha principal (ficha de identificación) del adolescente infractor")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        FichaIdentificacionDocumentoDTO fichaIdentificacionDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaIdentificacionDocumentoDTO.class);
        RespuestaPorDefectoAuditoria<FichaIdentificacionDocumentoDTO> df = this.fichaIdentificacionDocumentoService.eliminarRelacionConDocumento(
                httpServletRequest, fichaIdentificacionDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_PRINCIPAL_ELIMINACION_DOCUMENTOS
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
