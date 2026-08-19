package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;

import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SancionDisciplinariaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.SancionDisciplinariaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/sancion-disciplinaria")
@SecurityRequirement(name = "Authorization")
public class SancionDisciplinariaController {
    private final SancionDisciplinariaService sancionDisciplinariaService;
    private final AuditoriaAccionesSistemaService auditoriaService;
    private final ParametroDelSistemaRepository parametroRepository;


    @PostMapping("/crear")
    @Operation(summary = "Crea o edita una sanción disciplinaria")
    public ResponseEntity<BodyEncriptado> crearSancion(HttpServletRequest request, @RequestBody BodyEncriptado body) throws Exception {
        Date inicio = new Date();
        String bodyDesencriptado = body.desencriptarPorEmpresa(parametroRepository, null).getData();
        RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> respuesta = sancionDisciplinariaService.crearSancion(request, body);
        String accion = (respuesta.getData() != null && Boolean.TRUE.equals(respuesta.getData().getEsEdicion()))
                ? EtiquetaNemonico.ACCION_ACTUALIZAR_SANCION_DISCIPLINARIA
                : EtiquetaNemonico.ACCION_CREAR_SANCION_DISCIPLINARIA;
        auditoriaService.guardarAccionRequestEncriptado(request, bodyDesencriptado, respuesta, inicio, accion);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(parametroRepository, null));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar sanción por token identificador")
    public ResponseEntity<BodyEncriptado> buscarPorToken(HttpServletRequest request, @RequestParam String ID) throws Exception {
        RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> respuesta = sancionDisciplinariaService.obtenerSancionPorToken(request, ID);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(parametroRepository, null));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminar sanción disciplinaria")
    public ResponseEntity<BodyEncriptado> eliminar(HttpServletRequest request, @RequestBody BodyEncriptado body) throws Exception {
        Date inicio = new Date();
        String bodyDesencriptado = body.desencriptarPorEmpresa(parametroRepository, null).getData();
        RespuestaPorDefectoAuditoria<Boolean> respuesta = sancionDisciplinariaService.eliminarSancion(request, body);
        auditoriaService.guardarAccionRequestEncriptado(request, bodyDesencriptado, respuesta, inicio, EtiquetaNemonico.ACCION_ELIMINAR_SANCION_DISCIPLINARIA);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(parametroRepository, null));
    }

    @PostMapping("/listado/token")
    @Operation(summary = "Obtiene las sanciones disciplinarias paginadas por token de ficha")
    public ResponseEntity<BodyEncriptado> listarPorToken(
            HttpServletRequest request,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();

        String bodyDesencriptado = bodyEncriptado
                .desencriptarPorEmpresa(this.parametroRepository, null)
                .getData();


        RespuestaPorDefectoAuditoria<PaginacionResponse<SancionDisciplinariaDTO>> respuesta =
                sancionDisciplinariaService.obtenerListadoPorToken(request, bodyEncriptado);


        this.auditoriaService.guardarAccionRequestEncriptado(
                request,
                bodyDesencriptado,
                respuesta,
                fechaInicio,
                EtiquetaNemonico.ACCION_LISTAR_REGISTRO_SALIDA // usa tu nemónico correcto aquí
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroRepository, null));
    }

    @PostMapping("/subirDocumentos")
    @Operation(summary = "Sube uno o varios documentos")
    public ResponseEntity<BodyEncriptado> subirDocumentos(HttpServletRequest httpServletRequest,
                                                          @RequestParam("documentos") MultipartFile[] multipartFiles,
                                                          @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.sancionDisciplinariaService.subirDocumentos(httpServletRequest, bodyEncriptado, multipartFiles);

        this.auditoriaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_DOCUMENTO_SUBIDA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroRepository, null));
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados a la ficha y carpeta")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.sancionDisciplinariaService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_DOCUMENTO_OBTENER);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroRepository, null));
    }
}
