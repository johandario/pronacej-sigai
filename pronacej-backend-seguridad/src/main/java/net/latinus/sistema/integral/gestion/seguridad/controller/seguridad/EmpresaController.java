package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.EmpresaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/empresa")
@SecurityRequirement(name = "Authorization")
public class EmpresaController {

    private EmpresaService empresaService;


    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea una empresa para el sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<EmpresaDTO>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                                 @RequestBody EmpresaDTO empresaDTO) {

        return ResponseEntity.ok(this.empresaService.crearEmpresaDirecto(httpServletRequest, empresaDTO));
    }

}
