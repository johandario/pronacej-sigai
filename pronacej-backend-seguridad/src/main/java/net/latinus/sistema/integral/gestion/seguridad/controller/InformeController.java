package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.CampoInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.InformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.PlantillaInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.general.InformeService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/informe")
@SecurityRequirement(name = "Authorization")
public class InformeController {

    private InformeService informeService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerInformes")
    @Operation(summary = "Obtiene el listado de informes")
    public ResponseEntity<BodyEncriptado> obtenerInformes(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> df = this.informeService.obtenerInformes(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LISTA_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerInformesPorToken")
    @Operation(summary = "Obtiene el listado de informes por token del adolescente")
    public ResponseEntity<BodyEncriptado> obtenerInformesPorToken(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> df = this.informeService.obtenerInformesPorToken(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerInformePorId")
    @Operation(summary = "Obtiene un informe especifico por Id")
    public ResponseEntity<BodyEncriptado> obtenerInformePorId(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<InformeDTO> df = this.informeService.obtenerInformePorId(httpServletRequest, new Gson().fromJson(body, InformeDTO.class));
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearInforme")
    @Operation(summary = "Crea o edita un informe")
    public ResponseEntity<BodyEncriptado> crearInforme(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();

        // Desencriptar el body
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el body a InformeDTO para determinar si es creación o edición
        ObjectMapper objectMapper = new ObjectMapper();
        InformeDTO informeDTO = objectMapper.readValue(body, InformeDTO.class);

        // Llamar al servicio con el DTO deserializado
        RespuestaPorDefectoAuditoria<InformeDTO> respuesta = informeService.crearInforme(httpServletRequest, informeDTO);

        // ✅ MEJORA: Lógica mejorada para determinar crear vs editar
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basado en múltiples criterios
        if (informeDTO.getIdInforme() > 0) {
            esEdicion = true;
        } else if (informeDTO.getTokenIdentificador() != null && 
                   !informeDTO.getTokenIdentificador().isEmpty() && 
                   !informeDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_INFORME;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_INFORME;
        }

        // Guardar auditoría con la acción determinada
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, body, respuesta, fechaRequest, accionAuditoria
        );

        return ResponseEntity.ok(
                respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null)
        );
    }

    @PostMapping("/crearInformePorToken")
    @Operation(summary = "Crea o edita un informe por token")
    public ResponseEntity<BodyEncriptado> crearInformePorToken(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        // Deserializar el DTO para determinar si es creación o edición
        InformeDTO informeDTO = new Gson().fromJson(body, InformeDTO.class);

        RespuestaPorDefectoAuditoria<InformeDTO> df = this.informeService.crearInformePorToken(httpServletRequest, informeDTO);

        // ✅ MEJORA: Lógica mejorada para determinar crear vs editar
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basado en múltiples criterios
        if (informeDTO.getIdInforme() > 0) {
            esEdicion = true;
        } else if (informeDTO.getTokenIdentificador() != null && 
                   !informeDTO.getTokenIdentificador().isEmpty() && 
                   !informeDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_INFORME;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_INFORME;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirInformeFirmado")
    @Operation(summary = "Sube un informe firmado")
    public ResponseEntity<BodyEncriptado> subirInformeFirmado(HttpServletRequest httpServletRequest,
                                                              @RequestParam("documento") MultipartFile multipartFile,
                                                              @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.informeService.subirInformeFirmado(httpServletRequest, multipartFile, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_SUBIR_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados a la ficha y carpeta")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.informeService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_DOCUMENTO_OBTENER);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarInforme")
    @Operation(summary = "Actualiza un informe")
    public ResponseEntity<BodyEncriptado> actualizarInforme(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<InformeDTO> df = this.informeService.actualizarInforme(httpServletRequest, new Gson().fromJson(body, InformeDTO.class));
        
        // ✅ Este endpoint es específicamente para editar, así que siempre usa EDITAR
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarInforme")
    @Operation(summary = "Elimina un informe")
    public ResponseEntity<BodyEncriptado> eliminarInforme(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.informeService.removerInforme(httpServletRequest, new Gson().fromJson(body, InformeDTO.class));

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    // ===================================================================
    // MÉTODOS PARA PLANTILLAS DE INFORMES
    // ===================================================================

    @PostMapping("/obtenerListaPlantillas")
    @Operation(summary = "Obtiene el listado de plantillas de informes")
    public ResponseEntity<BodyEncriptado> obtenerListaPlantillas(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaInformeDTO>> df = this.informeService.obtenerListaPlantillasInforme(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_LISTA_PLANTILLA_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerPlantillas")
    @Operation(summary = "Obtiene el listado de plantillas de informes")
    public ResponseEntity<BodyEncriptado> obtenerPlantillas(HttpServletRequest httpServletRequest,
                                                            @RequestParam(required = false) String tokenCentro) throws Exception {

        RespuestaPorDefectoAuditoria<List<PlantillaInformeDTO>> df = this.informeService.obtenerPlantillasInforme(httpServletRequest, tokenCentro);

        // Para endpoints GET sin body encriptado, no se puede hacer auditoría completa
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearPlantilla")
    @Operation(summary = "Crea o edita una plantilla de informe")
    public ResponseEntity<BodyEncriptado> crearPlantilla(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        // Deserializar el DTO para determinar si es creación o edición
        PlantillaInformeDTO plantillaInformeDTO = new Gson().fromJson(body, PlantillaInformeDTO.class);

        RespuestaPorDefectoAuditoria<PlantillaInformeDTO> df = this.informeService.crearPlantillaInforme(httpServletRequest, plantillaInformeDTO);

        // ✅ MEJORA: Lógica mejorada para determinar crear vs editar plantilla
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basado en múltiples criterios
        if (plantillaInformeDTO.getIdPlantillaInforme() > 0) {
            esEdicion = true;
        } else if (plantillaInformeDTO.getTokenIdentificador() != null && 
                   !plantillaInformeDTO.getTokenIdentificador().isEmpty() && 
                   !plantillaInformeDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_PLANTILLA_INFORME;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_PLANTILLA_INFORME;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarPlantilla")
    @Operation(summary = "Actualiza una plantilla de informe")
    public ResponseEntity<BodyEncriptado> actualizarPlantilla(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.informeService.actualizarPlantillaInforme(httpServletRequest, bodyEncriptado);

        // ✅ Este endpoint es específicamente para editar, así que siempre usa EDITAR
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_PLANTILLA_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarPlantilla")
    @Operation(summary = "Elimina una plantilla de informe")
    public ResponseEntity<BodyEncriptado> eliminarPlantilla(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.informeService.removerPlantillaInforme(httpServletRequest, new Gson().fromJson(body, PlantillaInformeDTO.class));

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_PLANTILLA_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    // ===================================================================
    // MÉTODOS PARA CAMPOS DE PLANTILLAS
    // ===================================================================

    @PostMapping("/obtenerCamposPorIdPlantilla")
    @Operation(summary = "Obtiene los campos de la plantilla especificada")
    public ResponseEntity<BodyEncriptado> obtenerCamposPorIdPlantilla(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> df = this.informeService.obtenerCamposPorIdPlantilla(httpServletRequest, new Gson().fromJson(body, PlantillaInformeDTO.class));
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_PLANTILLA_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCamposPorIdInforme")
    @Operation(summary = "Obtiene los campos con sus valores del informe especificado")
    public ResponseEntity<BodyEncriptado> obtenerCamposPorIdInforme(HttpServletRequest httpServletRequest,
                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> df = this.informeService.obtenerCamposPorIdInforme(httpServletRequest, new Gson().fromJson(body, InformeDTO.class));
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCamposPorNemonico")
    @Operation(summary = "Obtiene los campos de la plantilla especificada por nemónico")
    public ResponseEntity<BodyEncriptado> obtenerCamposPorNemonico(HttpServletRequest httpServletRequest,
                                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> df = this.informeService.obtenerCamposPorNemonico(httpServletRequest, new Gson().fromJson(body, String.class));
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_PLANTILLA_INFORME);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}