package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefecto;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.UserDataResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.UsuarioSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import net.latinus.sistema.integral.gestion.seguridad.model.request.CargaMasivaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.CargaMasivaResponse;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Authorization")
@RequestMapping(path = "api/v1/usuario-sistema")
public class UsuarioSistemaController {

    private UsuarioSistemaService usuarioSistemaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
        
    @PostMapping("/carga-masiva")
    public ResponseEntity<RespuestaPorDefecto<CargaMasivaResponse>> subirUsuariosCargaMasiva(HttpServletRequest httpServletRequest,
            @RequestBody CargaMasivaRequest cargaMasivaRequest) {        
                
        return ResponseEntity.ok(this.usuarioSistemaService.subirUsuariosCargaMasiva(httpServletRequest, cargaMasivaRequest).transformarARespuestaPorDefecto());
    }

    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea un usuario directamente en el sistema")
    public ResponseEntity<RespuestaPorDefecto<UsuarioSistemaDTO>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                               @RequestBody UsuarioSistemaDTO usuarioSistemaDTO) {

        return ResponseEntity.ok(this.usuarioSistemaService.crearUsuarioDirecto(httpServletRequest, usuarioSistemaDTO).transformarARespuestaPorDefecto());
    }

    @GetMapping("/obtenerDataDelUsuarioLogeado")
    @Operation(summary = "Obten información del usuario del sistema logeado")
    public ResponseEntity<BodyEncriptado> obtenerDataDelUsuarioLogeado(HttpServletRequest httpServletRequest) throws Exception {

        RespuestaPorDefectoAuditoria<UserDataResponse> df = this.usuarioSistemaService.obtenerDataDelUsuarioLogeado(httpServletRequest);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarDatos")
    @Operation(summary = "Actualiza los datos de un usuario")
    public ResponseEntity<BodyEncriptado> actualizarDatos(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();


        RespuestaPorDefectoAuditoria<UserDataResponse> df = this.usuarioSistemaService.actualizarDatosDePerfilDelUsuario(httpServletRequest,
                bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, df,
                fechaRequest, EtiquetaNemonico.ACCION_ACTUALIZAR_DATOS_USUARIO_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
