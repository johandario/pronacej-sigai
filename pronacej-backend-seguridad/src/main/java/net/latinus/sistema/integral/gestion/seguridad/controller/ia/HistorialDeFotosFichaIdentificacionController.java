package net.latinus.sistema.integral.gestion.seguridad.controller.ia;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionTipoDeDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.HistorialDeFotosFichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.HistorialDeFotosFichaIdentificacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.HistorialDeFotosFichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/historial-de-fotos-ficha-identificacion")
@SecurityRequirement(name = "Authorization")
public class HistorialDeFotosFichaIdentificacionController {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private HistorialDeFotosFichaIdentificacionService historialDeFotosFichaIdentificacionService;

    @PostMapping("subir-archivos")
    @Operation(summary = "Sube documentos al historial de imagenes de la ficha de identificacion")
    public ResponseEntity<BodyEncriptado> subirArchivos(HttpServletRequest httpServletRequest,
                                                        @RequestParam("documento") MultipartFile multipartFile,
                                                        @RequestParam("body") String bodyEncriptadoString) throws Exception {
        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();;
        HistorialDeFotosFichaIdentificacionDTO historialDeFotosFichaIdentificacionDTO =
                new Gson().fromJson(bodyDesencriptado, HistorialDeFotosFichaIdentificacionDTO.class);

        RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> df =
                this.historialDeFotosFichaIdentificacionService.subirArchivoAlHistorial(httpServletRequest,
                        multipartFile, historialDeFotosFichaIdentificacionDTO);

        String accion = historialDeFotosFichaIdentificacionDTO.getEsEdicion() ? EtiquetaNemonico.ACCION_HISTORIAL_FOTOS_EDITAR : EtiquetaNemonico.ACCION_HISTORIAL_FOTOS_SUBIR;
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaRequest,
                accion

        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("obtener")
    @Operation(summary = "Obtener todos los historiales de imagenes de la ficha de identificacion")
    public ResponseEntity<BodyEncriptado> obtener(HttpServletRequest httpServletRequest,
                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();;
        HistorialDeFotosFichaIdentificacionRequest historialDeFotosFichaIdentificacionRequest =
                new Gson().fromJson(bodyDesencriptado, HistorialDeFotosFichaIdentificacionRequest.class);

        RespuestaPorDefectoAuditoria<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>> df =
                this.historialDeFotosFichaIdentificacionService.obtener(httpServletRequest, historialDeFotosFichaIdentificacionRequest);

        String accion = EtiquetaNemonico.ACCION_HISTORIAL_FOTOS_OBTENER;
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaRequest,
                accion
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("eliminar")
    @Operation(summary = "Elimina documentos del historial de imagenes de la ficha de identificacion")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();;
        HistorialDeFotosFichaIdentificacionDTO historialDeFotosFichaIdentificacionDTO =
                new Gson().fromJson(bodyDesencriptado, HistorialDeFotosFichaIdentificacionDTO.class);

        RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> df =
                this.historialDeFotosFichaIdentificacionService.eliminarRelacionConElDocumento(httpServletRequest,
                        historialDeFotosFichaIdentificacionDTO);

        String accion = EtiquetaNemonico.ACCION_HISTORIAL_FOTOS_ELIMINAR;
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaRequest,
                accion
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("obtenerFotoPerfil")
    @Operation(summary = "Obtiene foto frontal de fichaIdentificacion")
    public ResponseEntity<BodyEncriptado> obtenerFotoPerfil(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> df =
                this.historialDeFotosFichaIdentificacionService.obtenerFotoPerfil(httpServletRequest,
                        bodyEncriptado);

//        String accion = EtiquetaNemonico.ACCION_HISTORIAL_FOTOS_ELIMINAR;
//        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
//                httpServletRequest, bodyDesencriptado, df, fechaRequest,
//                accion
//        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
