package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.PasswordUserSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.ReseteoDePasswordDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.LoginRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.CambioDeContraseniaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ReseteoDeContraseniaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.security.PasswordEncoder;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReseteoDePasswordServiceImpl implements ReseteoDePasswordService {

    private RecaptchaService recaptchaService;
    private UsuarioSistemaRepository usuarioSistemaRepository;
    private UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository;
    private EmailService emailService;
    private CatalogoRepository catalogoRepository;
    private ReseteoDePasswordRepository reseteoDePasswordRepository;
    private PasswordUserSistemaRepository passwordUserSistemaRepository;
    private PasswordEncoder passwordEncoder;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private MenuRepository menuRepository;
    private FuncionarioJerarquiaRolRepository asignacionRepo;

    @Autowired
    public ReseteoDePasswordServiceImpl(RecaptchaService recaptchaService,
                                        UsuarioSistemaRepository usuarioSistemaRepository,
                                        UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository,
                                        EmailService emailService, CatalogoRepository catalogoRepository,
                                        ReseteoDePasswordRepository reseteoDePasswordRepository,
                                        PasswordUserSistemaRepository passwordUserSistemaRepository,
                                        PasswordEncoder passwordEncoder, ParametroDelSistemaRepository parametroDelSistemaRepository,
                                        MenuRepository menuRepository,FuncionarioJerarquiaRolRepository asignacionRepo) {

        this.recaptchaService = recaptchaService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.usuarioSistemaEmpresaRolRepository = usuarioSistemaEmpresaRolRepository;
        this.emailService = emailService;
        this.catalogoRepository = catalogoRepository;
        this.reseteoDePasswordRepository = reseteoDePasswordRepository;
        this.passwordUserSistemaRepository = passwordUserSistemaRepository;
        this.passwordEncoder = passwordEncoder;
        this.parametroDelSistemaRepository = parametroDelSistemaRepository;
        this.menuRepository = menuRepository;
        this.asignacionRepo = asignacionRepo;
    }

    @Value("${urlFront}")
    private String urlFront;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> empezarAccionDeReseteoDePassword(HttpServletRequest httpServletRequest,
                                                                                               ReseteoDeContraseniaRequest reseteoDeContraseniaRequest) {
        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<Boolean> df2 = this.recaptchaService.verificarRecaptchaV3(reseteoDeContraseniaRequest.getRecaptchaV3(), null);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                return df;
            }

            List<UsuarioSistema> usuarioSistemaList = this.usuarioSistemaRepository.findByEmailAndRemovido(reseteoDeContraseniaRequest.getEmail(),
                    false);

            if (usuarioSistemaList.isEmpty()) {
                df.setMensaje("No se ha encontrado un usuario válido con el correo enviado");
                return df;
            }

            if (usuarioSistemaList.size() > 1) {
                df.setMensaje("Hay más de un usuario con el mismo username registrado, consulta a tu administrador");
                return df;
            }

            UsuarioSistema usuarioSistema = usuarioSistemaList.getFirst();

            if (usuarioSistema.getBloqueado() != null && usuarioSistema.getBloqueado()) {
                df.setMensaje("Tu usuario está bloqueado");
                return df;
            }

            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository.
                    findByUsuarioSistemaTokenIdentificadorAndRemovido(
                            usuarioSistema.getTokenIdentificador(), false
                    );

            if (usuarioSistemaList.isEmpty()) {
                df.setMensaje("Tu usuario no tiene establecida una empresa");
                return df;
            }

            Set<Empresa> empresaSet = usuarioSistemaEmpresaRolList.stream().map(
                    (uer) -> uer.getEmpresa()
            ).collect(Collectors.toSet());

            if (empresaSet.isEmpty()) {
                df.setMensaje("Tu usuario no tiene establecida una empresa");
                return df;
            }

            //No implementado para varias empresas
            if (empresaSet.size() > 1) {
                df.setMensaje("Tu usario tiene más de una empresa relacionada");
                return df;
            }
            ////////

            Empresa empresa = empresaSet.iterator().next();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            if (empresa.getBloqueado() != null && empresa.getBloqueado()) {
                df.setMensaje("Tu empresa esta bloqueada para ser usada");
                return df;
            }

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = usuarioSistemaEmpresaRolList.stream().filter(
                    (uer) -> uer.getEmpresa().getTokenIdentificador().equals(empresa.getTokenIdentificador())
            ).findFirst().orElse(null);

            if (usuarioSistemaEmpresaRol == null) {
                df.setMensaje("Tu usuario no esta asociado a una empresa");
                return df;
            }

            if (usuarioSistemaEmpresaRol.getBloqueado() != null && usuarioSistemaEmpresaRol.getBloqueado()) {
                df.setMensaje("Estás bloqueado para realizar operaciones");
                return df;
            }

//            Rol rol = usuarioSistemaEmpresaRol.getRol();
//
//            if (rol.getBloqueado() != null && rol.getBloqueado()) {
//                df.setMensaje("Tu rol está bloqueado para ser usado");
//                return df;
//            }

            Catalogo estadoInicial = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                    EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_PENDIENTE, empresa.getTokenIdentificador(), false
            );

            if (estadoInicial == null) {
                df.setMensaje("No se encontró un estado inicial para el reseteo, contacta a tu administrador");
                return df;
            }

            ReseteoDePassword reseteoDePassword = new ReseteoDePassword();

            Menu menuReseteoContrasenia = this.menuRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    empresa.getTokenIdentificador(),
                    EtiquetaNemonico.MENU_REESTABLECER_CONTRASENIA_REESTABLECER,
                    false
            );

            if (menuReseteoContrasenia == null) {
                df.setMensaje("No se ha configurado el menú de reestablecimiento de contraseña, contacta con tu administrador");
                return df;
            }

            Menu menuReseteoContraseniaCancelacion = this.menuRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    empresa.getTokenIdentificador(),
                    EtiquetaNemonico.MENU_REESTABLECER_CONTRASENIA_CANCELAR,
                    false
            );

            if (menuReseteoContraseniaCancelacion == null) {
                df.setMensaje("No se ha configurado el menú de reestablecimiento de contraseña para cancelar, contacta con tu administrador");
                return df;
            }

            String pathFront = menuReseteoContrasenia.getLink() + "?token=" + reseteoDePassword.getTokenIdentificador();
            String url = this.urlFront + pathFront;
            String urlCancelar = this.urlFront +
                    menuReseteoContraseniaCancelacion.getLink() + "?token=" + reseteoDePassword.getTokenIdentificador();

            String email = usuarioSistema.getEmail();
            Map<String, String> valores = new HashMap<>();
            String nombreCompleto = (usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos()).trim();
            valores.put("[NOMBRE_USUARIO]", nombreCompleto);
            valores.put("[NOMBRE_BOTON]", "Reestablecer contraseña");
            valores.put("[ENLACE]", url);
            valores.put("[ENLACE_CANCELAR]", urlCancelar);

            List<String> emailsTo = new ArrayList<>();
            emailsTo.add(email);

            RespuestaPorDefectoAuditoria<Boolean> df3 = this.emailService.enviarCorreoConTemplate(
                    EtiquetaNemonico.CORREO_CREACION_PROCESO_RESTABLECIMIENTO_PASSWORD, emailsTo
                    , empresa.getTokenIdentificador(),
                    valores
            );

            if (!df3.isExito()) {
                df.setMensaje("No se pudo enviar el correo a: " + email + ", debido a: " + df3.getMensaje());
                return df;
            }

            reseteoDePassword.setEmpresa(empresa);
            reseteoDePassword.setEstado(
                    estadoInicial
            );
            reseteoDePassword.setIpCrea(httpServletRequest.getRemoteAddr());
            reseteoDePassword.setUsuarioSistemaCrea(usuarioSistema);
            reseteoDePassword.setUsuarioSistemaEmpresaRol(usuarioSistemaEmpresaRol);
            this.reseteoDePasswordRepository.save(reseteoDePassword);

            FuncionesAyuda funcionesAyuda = new FuncionesAyuda();
            df.llenarRespuestaExitosa("Se ha enviado un correo electrónico a: " +
                    funcionesAyuda.cubrirPalabra(email,
                            "medio", 4) + " con los pasos a seguir para el restablecimiento de contraseña", reseteoDePassword.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> reseteoDePassword(HttpServletRequest httpServletRequest,
                                                                                ReseteoDePasswordDTO reseteoDePasswordDTO) {
        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {


            RespuestaPorDefectoAuditoria<Boolean> df2 = this.recaptchaService.verificarRecaptchaV3(reseteoDePasswordDTO.getRecaptchaV3(),
                    null);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                return df;
            }

            ReseteoDePassword reseteoDePasswordPendiente =
                    this.reseteoDePasswordRepository.findByTokenIdentificadorAndEstadoNemonicoAndRemovido(
                            reseteoDePasswordDTO.getTokenIdentificador(),
                            EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_PENDIENTE,
                            false
                    );

            if (reseteoDePasswordPendiente == null) {
                df.setMensaje("No se puede realizar el reseteo de contraseña debido a que este ya fue atendido anteriormente o no existe");
                return df;
            }

            Empresa empresa = reseteoDePasswordPendiente.getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df3 = this.verificarReseteoDePassword(httpServletRequest,
                    reseteoDePasswordDTO);

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            Catalogo estadoCompletado = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                    EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_COMPLETADO,
                    empresa.getTokenIdentificador(), false
            );

            if (estadoCompletado == null) {
                df.setMensaje("No existe un estado completado para el reseteo de contraseña");
                return df;
            }

            PasswordUserSistemaDTO passwordUserSistemaDTO = reseteoDePasswordDTO.getPasswordUserSistemaDTO();
            String password = passwordUserSistemaDTO.getPassword();
            String passswordConfirm = passwordUserSistemaDTO.getPasswordEncrypt();

            if (!passswordConfirm.equals(password)) {
                df.setMensaje("Las contraseñas enviadas no coinciden");
                return df;
            }

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = reseteoDePasswordPendiente.getUsuarioSistemaEmpresaRol();

            if (usuarioSistemaEmpresaRol == null) {
                df.setMensaje("El proceso no esta atado a un usuario válido (uer)");
                return df;
            }

            if (usuarioSistemaEmpresaRol.getBloqueado() != null && usuarioSistemaEmpresaRol.getBloqueado()) {
                df.setMensaje("Estás bloqueado para realizar operaciones");
                return df;
            }

            UsuarioSistema usuarioSistema = usuarioSistemaEmpresaRol.getUsuarioSistema();

            if (usuarioSistema == null) {
                df.setMensaje("El proceso no está atado a un usuario válido");
                return df;
            }

            if (usuarioSistema.getBloqueado() != null && usuarioSistema.getBloqueado()) {
                df.setMensaje("Tu usuario está bloqueado para realizar operaciones");
                return df;
            }

            Page<ParametroDelSistema> parametroDelSistemaPage = this.parametroDelSistemaRepository.findByNemonicoAndRemovidoAndEmpresaIdEmpresa(
                    EtiquetaNemonico.PARAM_AES_CLAVE, false, null, PageRequest.of(0, 3));

            if (parametroDelSistemaPage.isEmpty()) {
                df.setMensaje("No existe la clave de encriptación, comunícate con tu administrador");
                return df;
            }

            String aesClave = parametroDelSistemaPage.toList().get(0).getValor();

            if (aesClave == null || aesClave.isEmpty() || aesClave.isBlank()) {
                df.setMensaje("La clave de encriptación es inválida, comunícate con tu administrador");
                return df;
            }

            List<PasswordUserSistema> passwordUserSistemaList = this.passwordUserSistemaRepository.findByUsuarioSistemaIdUsuarioSistemaAndRemovido(
                    usuarioSistema.getIdUsuarioSistema(), false
            );

            String ip = httpServletRequest.getRemoteAddr();
            Date fechaActual = new Date();

            //Removiendo las contraseñas anteriores
            for (PasswordUserSistema passwordUserSistema : passwordUserSistemaList) {
                passwordUserSistema.setRemovido(true);
                passwordUserSistema.setFechaEliminacion(fechaActual);
                passwordUserSistema.setIpElimina(ip);
                passwordUserSistema.setUsuarioSistemaElimina(usuarioSistema);
                this.passwordUserSistemaRepository.save(passwordUserSistema);
            }

            String passwordEncoded = this.passwordEncoder.encode(password);
            String passwordAes = new Aes().encrypt(aesClave, password);

            PasswordUserSistema passwordUserSistema = new PasswordUserSistema();
            passwordUserSistema.setPassword(passwordEncoded);
            passwordUserSistema.setPasswordEncrypt(passwordAes);
            passwordUserSistema.setUsuarioSistemaCrea(usuarioSistema);
            passwordUserSistema.setIpCrea(ip);
            passwordUserSistema.setUsuarioSistema(usuarioSistema);
            this.passwordUserSistemaRepository.save(passwordUserSistema);

            reseteoDePasswordPendiente.setEstado(
                    estadoCompletado
            );

            reseteoDePasswordPendiente.setIpEdita(ip);
            reseteoDePasswordPendiente.setUsuarioSistemaEdita(usuarioSistema);
            reseteoDePasswordPendiente.setFechaEdicion(fechaActual);
            this.reseteoDePasswordRepository.save(reseteoDePasswordPendiente);

            df.llenarRespuestaExitosa("Contraseña reseteada con exito, ya puedes usarla en tu nuevo logeo",
                    reseteoDePasswordPendiente.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> cancelarReseteo(HttpServletRequest httpServletRequest,
                                                                              ReseteoDePasswordDTO reseteoDePasswordDTO) {

        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {


            String tokenIdentificador = reseteoDePasswordDTO.getTokenIdentificador();

            ReseteoDePassword reseteoDePasswordPendiente =
                    this.reseteoDePasswordRepository.findByTokenIdentificadorAndEstadoNemonicoAndRemovido(
                            tokenIdentificador,
                            EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_PENDIENTE,
                            false
                    );

            if (reseteoDePasswordPendiente == null) {
                df.setMensaje("No se puede realizar la cancelación del reseteo de contraseña debido a que este ya fue atendido anteriormente o no existe");
                return df;
            }

            Empresa empresa = reseteoDePasswordPendiente.getEmpresa();

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            Catalogo estadoCancelado = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                    EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_CANCELADO,
                    empresa.getTokenIdentificador(), false
            );

            if (estadoCancelado == null) {
                df.setMensaje("No existe el estado cancelado para el reseteo, consulta a tu administrador");
                return df;
            }

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = reseteoDePasswordPendiente.getUsuarioSistemaEmpresaRol();

            if (usuarioSistemaEmpresaRol == null) {
                df.setMensaje("El proceso no está atado a un usuario válido (er)");
                return df;
            }

            if (usuarioSistemaEmpresaRol.getBloqueado() != null && usuarioSistemaEmpresaRol.getBloqueado()) {
                df.setMensaje("Estás bloqueado para realizar operaciones");
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();
            Date fechaActual = new Date();
            UsuarioSistema usuarioSistema = reseteoDePasswordPendiente.getUsuarioSistemaCrea();

            reseteoDePasswordPendiente.setEstado(
                    estadoCancelado
            );

            reseteoDePasswordPendiente.setIpEdita(ip);
            reseteoDePasswordPendiente.setUsuarioSistemaEdita(usuarioSistema);
            reseteoDePasswordPendiente.setFechaEdicion(fechaActual);
            this.reseteoDePasswordRepository.save(reseteoDePasswordPendiente);

            df.llenarRespuestaExitosa("Se ha cancelado correctamente el proceso de restablecimiento de contraseña",
                    reseteoDePasswordPendiente.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> verificarReseteoDePassword(HttpServletRequest httpServletRequest,
                                                                                         ReseteoDePasswordDTO reseteoDePasswordDTO) {

        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            ReseteoDePassword reseteoDePassword = this.reseteoDePasswordRepository.findByTokenIdentificadorAndRemovido(
                    reseteoDePasswordDTO.getTokenIdentificador(),
                    false
            );

            if (reseteoDePassword == null) {
                df.setMensaje("El reseteo de constraseña es inválido");
                return df;
            }

            Empresa empresa = reseteoDePassword.getEmpresa();

            if (empresa == null) {
                df.setMensaje("El reseteo de contraseña no tiene asociada una empresa");
                return df;
            }

            String pendienteEstado = EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_PENDIENTE;
            Catalogo estado = reseteoDePassword.getEstado();

            if (estado == null) {
                df.setMensaje("No se ha configurado un estado válido al proceso de reestablecimiento de contraseña");
                return df;
            }

            if (!pendienteEstado.equals(estado.getNemonico())) {
                df.setMensaje("El reseteo de contraseña ya ha sido atendido anteriomente, estado actual: " + estado.getNombre());
                return df;
            }

            Date fechaCreacion = reseteoDePassword.getFechaCreacion();
            String nemonicoMaxHoras = EtiquetaNemonico.PARAM_HORAS_MAX_ESPERA_RESETEO_DE_CONTRASENIA;
            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    nemonicoMaxHoras,
                    empresa, false
            );

            if (parametroDelSistema != null) {
                String valor = parametroDelSistema.getValor();
                if (valor == null || valor.isEmpty()) {
                    this.logService.warn("El parametro del sistema con nemónico: " +
                            nemonicoMaxHoras + " tiene un valor nulo o vacío.");
                } else {
                    Integer maxHoras = Integer.valueOf(valor);
                    Date fechaActual = new Date();
                    Long dateTimeCurrent = fechaActual.getTime();
                    Long dateTimeCreacion = fechaCreacion.getTime();

                    Long millisDiference = dateTimeCurrent - dateTimeCreacion;
                    Long horasDiference = ((millisDiference / 1000) / 60) / 60;

                    if (horasDiference > maxHoras) {
                        Catalogo estadoCaducado = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                                EtiquetaNemonico.NEMONICO_RESETEO_DE_CONTRASENIA_CADUCADO, empresa.getTokenIdentificador(),
                                false
                        );

                        reseteoDePassword.setFechaEdicion(fechaActual);
                        reseteoDePassword.setEstado(estadoCaducado);
                        reseteoDePassword.setIpEdita(httpServletRequest.getRemoteAddr());
                        this.reseteoDePasswordRepository.save(reseteoDePassword);
                        df.setMensaje("Tu proceso de reseteo de contraseña ha caducado");
                        df.setMensajeErrorReal("Se ha cambiado el estado a caducado del reseteo de contraseña: " +
                                reseteoDePassword.getIdReseteoDePassword());
                        this.logService.info(df.getMensaje());

                        return df;
                    }
                }
            } else {
                this.logService.warn("No se ha configurado un parametro de sistema con el nemonico: " +
                        nemonicoMaxHoras);
            }

            df.llenarRespuestaExitosa("Se verificó correctamente el reseteo de contraseña",
                    reseteoDePasswordDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
