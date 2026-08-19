package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DireccionPersonaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PersonaRelacionadaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.PersonaRelacionadaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/personaRelacionada")
@SecurityRequirement(name = "Authorization")
public class PersonaRelacionadaController {

    private PersonaRelacionadaService personaRelacionadaService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerPersonasRelacionadasPaginado")
    @Operation(summary = "Obtiene las personas relacionadas a la ficha por medio del token identificador.")
    public ResponseEntity<BodyEncriptado> obtenerFichasIdentificacionPaginado(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> df = this.personaRelacionadaService.obtenerPersonaRelacionada(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearPersonaRelacionada")
    @Operation(summary = "Crea o edita una persona que guarda relacion con la ficha identificacion")
    public ResponseEntity<BodyEncriptado> crearFicha(HttpServletRequest httpServletRequest,
                                                     @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        // Deserializar el DTO para determinar si es creación o edición
        PersonaRelacionadaDTO personaRelacionadaDTO = new Gson().fromJson(body, PersonaRelacionadaDTO.class);

        RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> df = this.personaRelacionadaService.crearPersonaRelacionada(httpServletRequest, personaRelacionadaDTO);

        // Determinar la acción de auditoría basada en si es una edición o creación
        String accionAuditoria;
        if (personaRelacionadaDTO.getTokenIdentificador() != null && !personaRelacionadaDTO.getTokenIdentificador().isEmpty()) {
            // Si tiene tokenIdentificador, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_PERSONA_RELACIONADA;
        } else {
            // Si no tiene tokenIdentificador, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_PERSONA_RELACIONADA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerPersonaRelacionada")
    @Operation(summary = "Obtiene una persona relacionada basada en su identificador")
    public ResponseEntity<BodyEncriptado> obtenerPersonaRelacionadaPorId(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> df = this.personaRelacionadaService.obtenerPersonaRelacionadaPorToken(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerPersonasRelacionadasPorEvaluacionSocial")
    @Operation(summary = "Obtiene todas las personas relacionadas en filtradas por una idEvaluacionSocial")
    public ResponseEntity<BodyEncriptado> obtenerPersonasRelacionadasPorTokenIdentificadorEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> df = this.personaRelacionadaService.obtenerPersonasRelacionadasPorTokenIdentificadorEvaluacionSocial(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarPersonaRelacionada")
    @Operation(summary = "Elimina una persona relacionada")
    public ResponseEntity<BodyEncriptado> eliminarFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.personaRelacionadaService.eliminarPersonaRelacionada(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarPersonaRelacionadaPorSituacionEconomicaSocial")
    @Operation(summary = "Elimina una persona relacionada de una Evaluacion Social")
    public ResponseEntity<BodyEncriptado> eliminarPersonaRelacionadaPorSituacionEconomicaSocial(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.personaRelacionadaService.eliminarPersonaRelacionadaPorSituacionEconomicaSocial(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearDireccionPersonaRelacionada")
    @Operation(summary = "Crea una direccion que guarda relacion con una persona relacionada")
    public ResponseEntity<BodyEncriptado> crearDireccionPersonaRelacionada(HttpServletRequest httpServletRequest,
                                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<DireccionPersonaDTO> df = this.personaRelacionadaService.crearDireccionPersona(httpServletRequest,
                new Gson().fromJson(body, DireccionPersonaDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_DIRECCION_PERSONA_RELACIONADA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDireccionesRelacionadas")
    @Operation(summary = "Obtiene las personas relacionadas a la ficha por medio del token identificador.")
    public ResponseEntity<BodyEncriptado> obtenerDireccionesRelacionadas(HttpServletRequest httpServletRequest,
                                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<DireccionPersonaDTO>> df = this.personaRelacionadaService.obtenerDireccionesRelacionadas(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarDireccionRelacionada")
    @Operation(summary = "Elimina una persona relacionada")
    public ResponseEntity<BodyEncriptado> eliminarDireccionRelacionada(HttpServletRequest httpServletRequest,
                                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.personaRelacionadaService.eliminarDireccionRelacionada(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_DIRECCION_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/editarPersonaEnfermo")
    @Operation(summary = "Edita el atributo enfermo de la PersonaRelacionada")
    public ResponseEntity<BodyEncriptado> editarPersonaEnfermo(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> df = this.personaRelacionadaService.editarPersonaRelacionadaEnfermo(httpServletRequest, new Gson().fromJson(body, PersonaRelacionadaDTO.class));
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_PERSONA_RELACIONADA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerPersonasRelacionadasPorIdFicha")
    @Operation(summary = "Obtiene la lista de personas relacionadas del adolescente especificado")
    public ResponseEntity<BodyEncriptado> obtenerPersonasRelacionadasPorIdFicha(HttpServletRequest httpServletRequest,
                                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> df = this.personaRelacionadaService.obtenerPersonasRelacionadasPorIdFicha(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerPersonasRelacionadasPorTokenFicha")
    @Operation(summary = "Obtiene la lista de personas relacionadas del adolescente especificado")
    public ResponseEntity<BodyEncriptado> obtenerPersonasRelacionadasPorTokenFicha(HttpServletRequest httpServletRequest,
                                                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> df = this.personaRelacionadaService.
                obtenerPersonasRelacionadasPorTokenIdenficadorFicha(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_DIRECCION_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/buscarPorNumeroDocumento")
    @Operation(summary = "Busca personas relacionadas por número de documento")
    public ResponseEntity<BodyEncriptado> buscarPorNumeroDocumento(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                            .getData();

        RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> df = this.personaRelacionadaService.buscarPersonaRelacionadaPorNumeroDocumento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERSONA_RELACIONADA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
