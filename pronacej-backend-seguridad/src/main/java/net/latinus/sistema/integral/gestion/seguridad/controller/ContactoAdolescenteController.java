package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ContactoAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.ContactoAdolescenteService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/contacto-adolescente")
@SecurityRequirement(name = "Authorization")
public class ContactoAdolescenteController {

    private final ContactoAdolescenteService contactoAdolescenteService;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/lista")
    @Operation(summary = "Obtiene todos los contactos de manera paginada.")
    public ResponseEntity<BodyEncriptado> obtenerContactos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<ContactoAdolescenteDTO>> df = contactoAdolescenteService.obtenerContactos(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_BUSCAR_CONTACTO_ADOLESCENTE);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository,
                null));
    }


    @PostMapping("/crear")
    @Operation(summary = "Crea un nuevo contacto.")
    public ResponseEntity<BodyEncriptado> crearContactos(HttpServletRequest httpServletRequest,
                                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> respuesta = contactoAdolescenteService.crearContacto(httpServletRequest, bodyEncriptado);
        String accion;
        if (respuesta.getData() != null && Boolean.TRUE.equals(respuesta.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_ACTUALIZAR_CONTACTO_ADOLESCENTE;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_CONTACTO_ADOLESCENTE;
        }
        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, respuesta,
                fechaInicio, accion);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> obtenerContactoPorToken(HttpServletRequest httpServletRequest,
                                                                             @RequestParam String ID) throws Exception {
        RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> df = this.contactoAdolescenteService.obtenerContactosPorToken(httpServletRequest, ID);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/eliminar")
    @Operation(summary = "Elimina registro")
    public ResponseEntity<BodyEncriptado> eliminarContacto(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<Boolean> df = this.contactoAdolescenteService.eliminarContactos(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_CONTACTO_ADOLESCENTE);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
