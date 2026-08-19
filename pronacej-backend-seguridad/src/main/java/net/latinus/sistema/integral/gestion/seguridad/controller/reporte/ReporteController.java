package net.latinus.sistema.integral.gestion.seguridad.controller.reporte;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.AdolescenteExternadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioNombresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ReporteService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/reporte")
@SecurityRequirement(name = "Authorization")
public class ReporteController {
    private final ReporteService reporteService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerAdolescentesExternados")
    @Operation(summary = "Obtener reporte de adolescentes externados")
    public ResponseEntity<BodyEncriptado> obtenerAdolescentesExternados(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<AdolescenteExternadoDTO>> resp = this.reporteService.obtenerAdolescentesExternados(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, resp, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_REPORTE_ADOLESCENTES_EXTERNADOS);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
