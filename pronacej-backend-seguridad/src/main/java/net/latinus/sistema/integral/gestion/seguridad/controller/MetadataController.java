package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.LoginResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.MetadataService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/metadata")
@SecurityRequirement(name = "Authorization")
public class MetadataController {

    private MetadataService metadataService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @GetMapping("/obtenerTablas")
    public ResponseEntity<BodyEncriptado> obtenerTablasQueUsanFicha() throws Exception {

        RespuestaPorDefectoAuditoria<List<String>> df = metadataService.obtenerTablasQueUsanFicha();

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerCampos")
    public ResponseEntity<BodyEncriptado> obtenerCamposFecha(@RequestParam String tabla) throws Exception {

        RespuestaPorDefectoAuditoria<List<String>> df = metadataService.obtenerCamposFecha(tabla);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }
}
