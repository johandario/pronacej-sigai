package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ParametroDelSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefecto;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/parametro-del-sistema")
@SecurityRequirement(name = "Authorization")
public class ParametroDelSistemaController {

    private ParametroDelSistemaService parametroDelSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea un parametro del sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>>>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                                                                                 @RequestBody List<ParametroDelSistemaDTO> parametroDelSistemaDTOList) {
        return ResponseEntity.ok(this.parametroDelSistemaService.crearVariosDirecto(httpServletRequest, parametroDelSistemaDTOList));
    }

    @GetMapping("/obtenerParam2")
    @Operation(summary = "Obten un parametro del sistema")
    public ResponseEntity<RespuestaPorDefecto<String>> obtenerParam2(HttpServletRequest httpServletRequest,
                                                                     @RequestParam(name = "nemonico") String nemonico,
                                                                     @RequestParam(name = "tokenIdentificadorEmpresa") String tokenIdentificadorEmpresa) {
        RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> df = this.parametroDelSistemaService.
                encontrarPorNemonicoYEmpresa2(nemonico, tokenIdentificadorEmpresa);

        return ResponseEntity.ok(df.transformarARespuestaPorDefectoDataStringBase64());
    }


    @PostMapping("/obtenerParamHijos")
    @Operation(summary = "Crea un parametro del sistema con encriptación diferente")
    public ResponseEntity<BodyEncriptado> obtenerParam2(HttpServletRequest httpServletRequest,
                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<List<ParametroDelSistemaDTO>> df = this.parametroDelSistemaService.obtenerParametrosDelSistemaGenerales(
                httpServletRequest, bodyEncriptado
        );


        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository,
                null));
    }

    @GetMapping("/obtenerValorApp")
    @Operation(summary = "Obten el vaor de un parametro del sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<String>> obtenerValor(HttpServletRequest httpServletRequest,
                                                                             @RequestParam String nemonico) throws Exception {
        String tokenIdentificadorEmpresa = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_TOKEN_EMPRESA);
        RespuestaPorDefectoAuditoria<String> df = this.parametroDelSistemaService.obtenerValorParam(
                nemonico, null, true
        );

        return ResponseEntity.ok(df);
    }

    @GetMapping("/obtenerParametroDelSistema")
    @Operation(summary = "Obten el parametro de sistema por el nemonico")
    public ResponseEntity<BodyEncriptado> obtenerParametroDelSistema(HttpServletRequest httpServletRequest,
                                                                     @RequestParam String nemonico) throws Exception {
        RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> df = this.parametroDelSistemaService.obtenerPorNemonico(httpServletRequest,
                nemonico);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository,
                null));
    }

}
