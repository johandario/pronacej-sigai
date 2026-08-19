package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosDeSeguridadDeUsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PasswordUserSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.UsuarioSistemaEmpresaRolRepository;
import net.latinus.sistema.integral.gestion.seguridad.security.PasswordEncoder;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class PasswordUserSistemaServiceImpl implements PasswordUserSistemaService {

    private JwtProviderService jwtProviderService;

    private PasswordUserSistemaRepository passwordUserSistemaRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private PasswordEncoder passwordEncoder;

    private ParametroDelSistemaService parametroDelSistemaService;
    private RecaptchaService recaptchaService;
    private UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository;

    private final LogService logService = new LogService(PasswordUserSistemaServiceImpl.class);


    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarDatosDeSeguridad(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {


            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            DatosDeSeguridadDeUsuarioSistemaDTO cambioDePasswordRequest = new Gson().fromJson(bodyString, DatosDeSeguridadDeUsuarioSistemaDTO.class);

            RespuestaPorDefectoAuditoria<Boolean> df3 = this.recaptchaService.verificarRecaptchaV3(cambioDePasswordRequest.getRecaptchaV3(),
                    null);

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null);

            if (parametroDelSistema == null) {
                df.setMensaje("No existe la clave de encriptación, comunicate con tu administrador");
                return df;
            }

            String aesClave = parametroDelSistema.getValor();

            if (aesClave == null || aesClave.isEmpty() || aesClave.isBlank()) {
                df.setMensaje("La clave de encriptación es inválida, comunicate con tu administrador");
                return df;
            }

            if (!usuarioSistema.getTokenIdentificador().equals(cambioDePasswordRequest.getTokenIdentificador())) {
                df.setMensaje("La identificación del usuario no coincide con la de la sesión");
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();
            Date fechaActual = new Date();

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = df2.getData().getUsuarioSistemaEmpresaRol();
            usuarioSistemaEmpresaRol.setUsuarioSistemaEdita(usuarioSistema);
            usuarioSistemaEmpresaRol.setIpEdita(ip);
            usuarioSistemaEmpresaRol.setFechaEdicion(fechaActual);
            usuarioSistemaEmpresaRol.setAutenticacionEn2Pasos(cambioDePasswordRequest.getHabilitar2DoFactorDeAutenticacion());
            usuarioSistemaEmpresaRol.setCambioContraseniaCadaNDias(cambioDePasswordRequest.getCambioDeContraseniaCadaNDias());
            this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);

            String constraseniaActual = cambioDePasswordRequest.getPasswordActual();

            //Cambio de Contraseña
            if (constraseniaActual != null && !constraseniaActual.isEmpty()) {
                List<PasswordUserSistema> passwordUserSistemaList = this.passwordUserSistemaRepository.
                        findByUsuarioSistemaIdUsuarioSistemaAndRemovidoOrderByIdPasswordDesc(
                                usuarioSistema.getIdUsuarioSistema(), false
                        );

                if (passwordUserSistemaList.isEmpty()) {
                    df.setMensaje("No tienes una contraseña activa válida");
                    df.setMensajeErrorReal("La lista de constraseña esta vacia");
                    return df;
                }

                if (passwordUserSistemaList.size() > 1) {
                    this.logService.warn("El usuario: " + usuarioSistema.getUserName() + " tiene mas de una contraseña activa");
                }

                PasswordUserSistema passwordUserSistema = passwordUserSistemaList.get(0);
                String contraseniaDB = passwordUserSistema.getPassword();

                String contraseniaNueva = cambioDePasswordRequest.getPassword();
                String contraseniaNuevaRepetida = cambioDePasswordRequest.getPasswordConfirm();

                if (!this.passwordEncoder.matches(constraseniaActual, contraseniaDB)) {
                    df.setMensaje("La contraseña actual no coincide con la registrada");
                    return df;
                }

                if (!contraseniaNueva.equals(contraseniaNuevaRepetida)) {
                    df.setMensaje("La contraseña nueva no coincide con la contraseña confirmada");
                    return df;
                }

                String passwordEncrypt = (new Aes()).encrypt(aesClave, contraseniaNueva);

                passwordUserSistema.setPassword(this.passwordEncoder.encode(contraseniaNueva));
                passwordUserSistema.setPasswordEncrypt(passwordEncrypt);
                passwordUserSistema.setUsuarioSistemaEdita(usuarioSistema);
                passwordUserSistema.setIpEdita(ip);
                passwordUserSistema.setFechaEdicion(fechaActual);
                this.passwordUserSistemaRepository.save(passwordUserSistema);
            }


            df.llenarRespuestaExitosa("Datos de seguridad actualizados con éxito", true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
