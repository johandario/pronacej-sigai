package net.latinus.sistema.integral.gestion.seguridad.controller.ia;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionTipoDeDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.TipoDeArchivoSeccionFichaPrincipal;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.FichaIdentificacionTipoDeDocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ficha-identificacion-tipo-de-documento")
@SecurityRequirement(name = "Authorization")
public class FichaIdentificacionTipoDeDocumentoController {

    private FichaIdentificacionTipoDeDocumentoService fichaIdentificacionTipoDeDocumentoService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @GetMapping("/obtenerTiposDeDocumentos")
    @Operation(summary = "Obtén los tipos de documentos configurados por sección de la ficha de identificación")
    public ResponseEntity<BodyEncriptado> obtenerTiposDeDocumentos(HttpServletRequest httpServletRequest,
                                                                   @RequestParam String nemonicoSeccionFichaIdentificacion,
                                                                   @RequestParam Map<String, String> allRequestParams) throws Exception {
        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>> df =
                this.fichaIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentosDeUnaSeccionDeLaFichaPrincipal(httpServletRequest,
                        nemonicoSeccionFichaIdentificacion);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, allRequestParams.toString(), df, fechaRequest,
                EtiquetaNemonico.ACCION_OBTENER_TIPOS_DOCUMENTOS_POR_SECCION
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerResumen")
    @Operation(summary = "Obtén todas las sección de la ficha principal con la cantidad de tipos de documentos asignados")
    public ResponseEntity<BodyEncriptado> obtenerResumen(HttpServletRequest httpServletRequest) throws Exception {
        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<List<TipoDeArchivoSeccionFichaPrincipal>> df =
                this.fichaIdentificacionTipoDeDocumentoService.obtenerSeccionDefichaPrincipalConTotalDeTipoDeDocumentos(
                        httpServletRequest
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, "", df, fechaRequest,
                EtiquetaNemonico.ACCION_OBTENER_RESUMEN_FICHA_IDENTIFICACION_TIPO_DOCUMENTO
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerPorSeccionFichaPrincipal")
    @Operation(summary = "Obtén todos los tipos de documento de una sección específica de la ficha principal")
    public ResponseEntity<BodyEncriptado> obtenerPorSeccionFichaPrincipal(HttpServletRequest httpServletRequest,
                                                                          @RequestParam String tokenSeccionFichaPrincipal) throws Exception {
        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>> df =
                this.fichaIdentificacionTipoDeDocumentoService.obtenerPorSeccionFichaPrincipal(
                        httpServletRequest,
                        tokenSeccionFichaPrincipal
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, "", df, fechaRequest,
                EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION_TIPO_DOCUMENTO
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Crea una nueva relación entre una sección de la ficha principal y el tipo de documento")
    public ResponseEntity<BodyEncriptado> crear(HttpServletRequest httpServletRequest,
                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDecrypt = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO = new Gson().fromJson(bodyDecrypt,
                FichaIdentificacionTipoDeDocumentoDTO.class);
        
        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df =
                this.fichaIdentificacionTipoDeDocumentoService.crear(
                        httpServletRequest,
                        fichaIdentificacionTipoDeDocumentoDTO
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDecrypt, df, fechaRequest,
                EtiquetaNemonico.ACCION_CREAR_FICHA_IDENTIFICACION_TIPO_DOCUMENTO
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/editar")
    @Operation(summary = "Edita una relación entre una sección de la ficha principal y un documento")
    public ResponseEntity<BodyEncriptado> editar(HttpServletRequest httpServletRequest,
                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDecrypt = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO = new Gson().fromJson(bodyDecrypt,
                FichaIdentificacionTipoDeDocumentoDTO.class);
        
        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df =
                this.fichaIdentificacionTipoDeDocumentoService.editar(
                        httpServletRequest,
                        fichaIdentificacionTipoDeDocumentoDTO
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDecrypt, df, fechaRequest,
                EtiquetaNemonico.ACCION_EDITAR_FICHA_IDENTIFICACION_TIPO_DOCUMENTO
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Elimina una relación entre una sección de la ficha principal y un tipo de documento")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDecrypt = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO = new Gson().fromJson(bodyDecrypt,
                FichaIdentificacionTipoDeDocumentoDTO.class);
        
        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df =
                this.fichaIdentificacionTipoDeDocumentoService.eliminar(
                        httpServletRequest,
                        fichaIdentificacionTipoDeDocumentoDTO
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDecrypt, df, fechaRequest,
                EtiquetaNemonico.ACCION_ELIMINAR_FICHA_IDENTIFICACION_TIPO_DOCUMENTO
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearOEditar")
    @Operation(summary = "Crea o edita una relación entre una sección de la ficha principal y el tipo de documento")
    public ResponseEntity<BodyEncriptado> crearOEditar(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDecrypt = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        // Deserializar el DTO para determinar si es creación o edición
        FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO = new Gson().fromJson(bodyDecrypt,
                FichaIdentificacionTipoDeDocumentoDTO.class);

        // Determinar si es creación o edición basado en si tiene tokenIdentificador válido
        boolean esEdicion = fichaIdentificacionTipoDeDocumentoDTO.getTokenIdentificador() != null && 
                           !fichaIdentificacionTipoDeDocumentoDTO.getTokenIdentificador().trim().isEmpty() &&
                           !fichaIdentificacionTipoDeDocumentoDTO.getTokenIdentificador().equals("0");

        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df;
        String accionAuditoria;

        if (esEdicion) {
            // Es una edición
            df = this.fichaIdentificacionTipoDeDocumentoService.editar(
                    httpServletRequest,
                    fichaIdentificacionTipoDeDocumentoDTO
            );
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_FICHA_IDENTIFICACION_TIPO_DOCUMENTO;
        } else {
            // Es una creación
            df = this.fichaIdentificacionTipoDeDocumentoService.crear(
                    httpServletRequest,
                    fichaIdentificacionTipoDeDocumentoDTO
            );
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_FICHA_IDENTIFICACION_TIPO_DOCUMENTO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDecrypt, df, fechaRequest, accionAuditoria
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
