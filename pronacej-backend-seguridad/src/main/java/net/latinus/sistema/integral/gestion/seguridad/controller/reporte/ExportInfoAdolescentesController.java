package net.latinus.sistema.integral.gestion.seguridad.controller.reporte;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionEstadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionJobIniciadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ExportInfoAdolescentesService;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ExportacionAdolescentesJobService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping(path={"api/v1/reporte"})
@SecurityRequirement(name="Authorization")
public class ExportInfoAdolescentesController {
    private static final Logger log = LoggerFactory.getLogger(ExportInfoAdolescentesController.class);
    private static final int TAMANO_BUFFER_DESCARGA = 65536;
    private static final DateTimeFormatter FORMATO_NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private final ExportInfoAdolescentesService exportInfoAdolescentesService;
    private final ExportacionAdolescentesJobService exportacionAdolescentesJobService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping(value={"/exportarAdolescentes/iniciar"})
    @Operation(summary="Inicia la exportaci\u00f3n por lotes de informaci\u00f3n de adolescentes")
    public ResponseEntity<?> iniciarExportacionAdolescentes(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        try {
            String bodyDesencriptado = (String)bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
            RespuestaPorDefectoAuditoria<ExportacionJobIniciadoDTO> resp = this.exportInfoAdolescentesService.iniciarExportacion(httpServletRequest, bodyEncriptado);
            this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, resp, fechaInicio, "ACCION_EXPORTAR_ADOLESCENTES");
            return ResponseEntity.ok((Object)resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        catch (Exception e) {
            RespuestaPorDefectoAuditoria respError = new RespuestaPorDefectoAuditoria();
            respError.llenarConDatosDeException(e);
            this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, "", respError, fechaInicio, "ACCION_EXPORTAR_ADOLESCENTES");
            return ResponseEntity.ok((Object)respError.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
    }

    @PostMapping(value={"/exportarAdolescentes/estado"})
    @Operation(summary="Consulta el avance de un job de exportaci\u00f3n de adolescentes")
    public ResponseEntity<?> consultarEstadoExportacionAdolescentes(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<ExportacionEstadoDTO> resp = this.exportInfoAdolescentesService.consultarEstadoExportacion(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok((Object)resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping(value={"/exportarAdolescentes/listar"})
    @Operation(summary="Lista los jobs de exportaci\u00f3n de adolescentes existentes en el sistema")
    public ResponseEntity<?> listarExportacionesAdolescentes(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<List<ExportacionEstadoDTO>> resp = this.exportInfoAdolescentesService.listarExportaciones(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok((Object)resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping(value={"/exportarAdolescentes/cancelar"})
    @Operation(summary="Cancela un job propio de exportaci\u00f3n de adolescentes en curso")
    public ResponseEntity<?> cancelarExportacionAdolescentes(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Void> resp = this.exportInfoAdolescentesService.cancelarExportacion(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok((Object)resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping(value={"/exportarAdolescentes/descartar"})
    @Operation(summary="Descarta un job propio de exportaci\u00f3n de adolescentes ya finalizado")
    public ResponseEntity<?> descartarExportacionAdolescentes(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<Void> resp = this.exportInfoAdolescentesService.descartarExportacion(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok((Object)resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping(value={"/exportarAdolescentes/descargar"})
    @Operation(summary="Descarga en streaming el CSV de una exportaci\u00f3n de adolescentes ya completada")
    public ResponseEntity<StreamingResponseBody> descargarExportacionAdolescentes(@RequestParam(value="token") String token) {
        long tamano;
        Path archivo;
        try {
            archivo = this.exportacionAdolescentesJobService.resolverArchivoParaDescarga(token);
        }
        catch (NoSuchElementException e) {
            StreamingResponseBody cuerpoError = outputStream -> outputStream.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body((Object)cuerpoError);
        }
        try {
            tamano = Files.size(archivo);
        }
        catch (Exception e) {
            log.error("No fue posible leer el archivo de exportaci\u00f3n '{}': {}", (Object)archivo, (Object)e.getMessage());
            this.exportacionAdolescentesJobService.finalizarDescarga(token);
            StreamingResponseBody cuerpoError = outputStream -> outputStream.write("No fue posible descargar el archivo solicitado.".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body((Object)cuerpoError);
        }
        StreamingResponseBody cuerpo = outputStream -> {
            try (InputStream entrada = Files.newInputStream(archivo, new OpenOption[0]);){
                this.copiarEnStreaming(entrada, outputStream);
            }
            finally {
                this.exportacionAdolescentesJobService.finalizarDescarga(token);
            }
        };
        String nombreArchivo = "informacion_adolescentes_" + FORMATO_NOMBRE_ARCHIVO.format(LocalDateTime.now()) + ".zip";
        return ((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + nombreArchivo + "\""})).header("Content-Type", new String[]{"application/zip"})).header("Content-Length", new String[]{String.valueOf(tamano)})).body((Object)cuerpo);
    }

    private void copiarEnStreaming(InputStream entrada, OutputStream salida) throws IOException {
        int leidos;
        byte[] buffer = new byte[65536];
        while ((leidos = entrada.read(buffer)) != -1) {
            salida.write(buffer, 0, leidos);
        }
        salida.flush();
    }

    public ExportInfoAdolescentesController(ExportInfoAdolescentesService exportInfoAdolescentesService, ExportacionAdolescentesJobService exportacionAdolescentesJobService, ParametroDelSistemaRepository parametroDelSistemaRepository, AuditoriaAccionesSistemaService auditoriaAccionesSistemaService) {
        this.exportInfoAdolescentesService = exportInfoAdolescentesService;
        this.exportacionAdolescentesJobService = exportacionAdolescentesJobService;
        this.parametroDelSistemaRepository = parametroDelSistemaRepository;
        this.auditoriaAccionesSistemaService = auditoriaAccionesSistemaService;
    }
}
