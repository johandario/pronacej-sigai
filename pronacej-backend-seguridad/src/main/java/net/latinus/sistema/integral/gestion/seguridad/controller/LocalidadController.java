package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LocalidadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.param.LocalidadService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/localidad")
@SecurityRequirement(name = "Authorization")
public class LocalidadController {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private LocalidadService localidadService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerLocalidadesTipo")
    @Operation(summary = "Obten los catalogos hijos en el sistema")
    public ResponseEntity<BodyEncriptado> obtenerLocalidadesTipo(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = this.localidadService.obtenerLocalidadPorNemonicTipo(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LOCALIDAD);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerLocalidadesPorPadre")
    @Operation(summary = "Obten los catalogos hijos en el sistema")
    public ResponseEntity<BodyEncriptado> obtenerLocalidadesPorPadre(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = this.localidadService.obtenerLocalidadPorNemonicPadre(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LOCALIDAD);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerLocalidadPorUbigeo")
    @Operation(summary = "Obtiene la localidad y su ruta segun su codigoUbigeo")
    public ResponseEntity<BodyEncriptado> obtenerLocalidadPorUbigeo(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<LocalidadDTO> df = this.localidadService.obtenerLocalidadUbigeo(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_UBIGEO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerLocalidadesArbol")
    @Operation(summary = "Obten un arbol de las localidad")
    public ResponseEntity<BodyEncriptado> obtenerLocalidadesArbol(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = this.localidadService.obtenerArbolPorNemonicPadre(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LOCALIDAD);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerLocalidadPorTokenIdentificador")
    @Operation(summary = "Obtiene la localidad y su ruta segun su codigoUbigeo")
    public ResponseEntity<BodyEncriptado> obtenerLocalidadPorTokenIdentificador(HttpServletRequest httpServletRequest,
                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<LocalidadDTO> df = this.localidadService.obtenerLocalidadTokenIdentificador(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_UBIGEO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerDescendencia")
    @Operation(summary = "Obtener lista de localidades padres por descendencia")
    public ResponseEntity<BodyEncriptado> obtenerDescendencia(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificador) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = this.localidadService
                .obtenerDescendencia(httpServletRequest, tokenIdentificador);

        // Para GET con parámetros, el cuerpo es el parámetro
        String bodyAuditoria = "tokenIdentificador: " + tokenIdentificador;
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyAuditoria, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LOCALIDAD);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearLocalidad")
    @Operation(summary = "Crea o edita una localidad")
    public ResponseEntity<BodyEncriptado> crearLocalidad(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        LocalidadDTO localidadDTO = new Gson().fromJson(bodyDesencriptado, LocalidadDTO.class);

        RespuestaPorDefectoAuditoria<LocalidadDTO> df = this.localidadService.crearLocalidad(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en el tokenIdentificador
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basándose en el tokenIdentificador del DTO
        if (localidadDTO.getTokenIdentificador() != null && 
            !localidadDTO.getTokenIdentificador().trim().isEmpty() && 
            !localidadDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        // También verificar por campo específico de edición si existe
        if (!esEdicion && localidadDTO.getEsEdicion() != null && localidadDTO.getEsEdicion()) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_LOCALIDAD;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_LOCALIDAD;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/obtenerLocalidadPorNemonico")
    @Operation(summary = "Obtiene una localidad por su nemónico")
    public ResponseEntity<BodyEncriptado> obtenerLocalidadPorNemonico(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<LocalidadDTO> df = this.localidadService.obtenerLocalidadPorNemonico(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LOCALIDAD);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/editarLocalidad")
    @Operation(summary = "Editar una localidad existente (nombre, nemónico y ubigeo)")
    public ResponseEntity<BodyEncriptado> editarLocalidad(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        System.out.println(">>> ENTRÓ A editarLocalidad");
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<LocalidadDTO> df = this.localidadService.editarLocalidad(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_EDITAR_LOCALIDAD
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}