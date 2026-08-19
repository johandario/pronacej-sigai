package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ClasificacionEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ClasificacionEnfermedadService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/clasificacion-enfermedad")
@SecurityRequirement(name = "Authorization")
public class ClasificacionEnfermedadController {

    private ClasificacionEnfermedadService clasificacionEnfermedadService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerClasificacionEnfermerdades")
    @Operation(summary = "Obtener una lista de items referentes a CIE-10")
    public ResponseEntity<BodyEncriptado> obtenerClasificacionEnfermerdades(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<ClasificacionEnfermedadDTO>> df = this.clasificacionEnfermedadService.obtenerClasificacionEnfermerdades(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CLASIFICACION_ENFERMEDADES
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

}