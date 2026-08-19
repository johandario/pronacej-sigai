package net.latinus.sistema.integral.gestion.seguridad.controller.documento;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/documento")
@SecurityRequirement(name = "Authorization")
public class DocumentosController {

    private DocumentoService documentoService;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final Aes aes = new Aes();;

    @PostMapping("/subir-documento")
    @Operation(summary = "Sube un archivo al sistema de alfresco")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {
        Date fechaInicio = new Date();
        BodyEncriptado bodyEncriptdo = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String request = bodyEncriptdo.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<DocumentoDTO> df = documentoService.subirDocumentoAlfresco(
                httpServletRequest, "", multipartFile,
                new Gson().fromJson(request, DocumentoDTO.class));

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                request, df, fechaInicio, EtiquetaNemonico.ACCION_DOCUMENTO_SUBIDA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizar-documento")
    @Operation(summary = "Actualiza un archivo al sistema de alfresco")
    public ResponseEntity<BodyEncriptado> actualizarDocumento(HttpServletRequest httpServletRequest,
                                                              @RequestParam("documento") MultipartFile multipartFile,
                                                              @RequestParam("body") String bodyEncriptadoString) throws Exception {
        Date fechaInicio = new Date();
        BodyEncriptado bodyEncriptdo = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String request = bodyEncriptdo.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();
        DocumentoDTO documentoDTO = new Gson().fromJson(request, DocumentoDTO.class);

        RespuestaPorDefectoAuditoria<DocumentoDTO> df = documentoService.actualizardocumento(
                httpServletRequest, multipartFile,
                documentoDTO);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                request, df, fechaInicio, EtiquetaNemonico.ACCION_DOCUMENTO_SUBIDA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerDocumento")
    @Operation(summary = "Obten un documento fisico subido al servidor de Alfresco")
    public ResponseEntity obtenerDocumento(HttpServletRequest httpServletRequest,
                                           @RequestParam String tokenIdentificadorDocumento,
                                           @RequestParam Map<String, String> allRequestParams) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<Resource> df = documentoService.obtenerDocumentoFisico(httpServletRequest,
                tokenIdentificadorDocumento);
        Resource resource = df.getData();
        df.setData(null);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                allRequestParams.toString(), df, fechaInicio, EtiquetaNemonico.ACCION_DOCUMENTO_OBTENER);

        if (df.isExito()) {
            return ResponseEntity.ok(resource);
        } else {
            return ResponseEntity.status(df.getCodigoEstado()).body(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
        }
    }

    @PostMapping("/eliminarDocumento")
    @Operation(summary = "Elimina un documento de la base de datos")
    public ResponseEntity<BodyEncriptado> eliminarDocumento(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();
        DocumentoDTO documentoDTO = new Gson().fromJson(bodyDesencriptado, DocumentoDTO.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = documentoService.eliminarDocumento(httpServletRequest,
                documentoDTO.getTokenIdentificador());

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_DOCUMENTO_SUBIDA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));

    }
}
