package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.PasswordUserSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ActualizacionDatosUsuarioRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.UserDataResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PasswordUserSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.UsuarioSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.security.PasswordEncoder;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.model.request.CargaMasivaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.CargaMasivaResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FuncionarioRepository;

import java.util.logging.Logger;

import net.latinus.sistema.integral.gestion.seguridad.entities.CargosJerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistemaEmpresaRol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.UsuarioCargaMasivaRequest;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.CargosJerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.RolRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.UsuarioSistemaEmpresaRolRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Service
@Transactional
@AllArgsConstructor
public class UsuarioSistemaServiceImpl implements UsuarioSistemaService {

    private UsuarioSistemaRepository usuarioSistemaRepository;

    private CatalogoRepository catalogoRepository;

    private FuncionarioRepository funcionarioRepository;

    private PasswordEncoder passwordEncoder;

    private ParametroDelSistemaService parametroDelSistemaService;

    private PasswordUserSistemaRepository passwordUserSistemaRepository;

    private JwtProviderService jwtProviderService;

    private RecaptchaService recaptchaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private JerarquiaRepository jerarquiaRepository;

    private CargosJerarquiaRepository cargosJerarquiaRepository;

    private RolRepository rolRepository;

    private EmpresaRepository empresaRepository;

    private UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository;

    private FuncionarioServiceImpl funcionarioServiceImpl;

    private EmailService emailService;

    private final FuncionesAyuda funcionesAyuda = new FuncionesAyuda();
    private static final Logger logger = Logger.getLogger(UsuarioSistemaServiceImpl.class.getName());

    @Override
    public RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> crearUsuario(UsuarioSistemaDTO usuarioSistemaDTO, UsuarioSistema usuarioQueCrea,
                                                                        String ipQueCrea) {

        RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = usuarioSistemaDTO.chequearValoresRequeridos();

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                return df;
            }

            List<UsuarioSistema> usuarioSistemaList = this.usuarioSistemaRepository.findByUserNameAndRemovido(
                    usuarioSistemaDTO.getUserName(), false
            );

            if (!usuarioSistemaList.isEmpty() && !usuarioSistemaDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el username: " + usuarioSistemaDTO.getUserName() + " elige otro para continuar");
                return df;
            }

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null);

            if (parametroDelSistema == null) {
                df.setMensaje("No existe la clave de encriptación, comunicate con tu administrador");
                return df;
            }

            String aesClave = parametroDelSistema.getValor();

            if (aesClave == null || aesClave.isBlank()) {
                df.setMensaje("La clave de encriptación es inválida, comunicate con tu administrador");
                return df;
            }

            UsuarioSistema usuarioSistema;
            if (usuarioSistemaDTO.getEsEdicion()) {
                usuarioSistema = this.usuarioSistemaRepository.findByTokenIdentificadorAndRemovido(usuarioSistemaDTO.getTokenIdentificador(), false);
                if (usuarioSistema == null) {
                    df.setMensaje("El usuario que estas editando no existe");
                    return df;
                }

                usuarioSistema.setFechaEdicion(new Date());
                usuarioSistema.setIpEdita(ipQueCrea);
                usuarioSistema.setUsuarioSistemaEdita(usuarioQueCrea);
            } else {
                usuarioSistema = new UsuarioSistema();
                usuarioSistema.setIpCrea(ipQueCrea);
                usuarioSistema.setUsuarioSistemaCrea(usuarioQueCrea);

                Funcionario funcionario = funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(usuarioSistemaDTO.getNumeroDeDocumento(), Boolean.FALSE, Boolean.FALSE);
                if (funcionario != null) {
                    usuarioSistema.setFuncionario(funcionario);
                }
            }
            usuarioSistema.setApellidos(usuarioSistemaDTO.getApellidos());
            usuarioSistema.setEmail(usuarioSistemaDTO.getEmail());
            usuarioSistema.getFuncionario().setEmail(usuarioSistemaDTO.getEmail());
            usuarioSistema.setNombres(usuarioSistemaDTO.getNombres());
            usuarioSistema.setNumeroDeCelular(usuarioSistemaDTO.getNumeroDeCelular());
            usuarioSistema.setNumeroDeDocumento(usuarioSistemaDTO.getNumeroDeDocumento());
            usuarioSistema.setTelefono(usuarioSistemaDTO.getTelefono());
            Catalogo tipoDeDocumento = catalogoRepository.findByTokenIdentificadorAndRemovido(usuarioSistemaDTO.getTokenIdentificadorTipoDeDocumento(), Boolean.FALSE);
            usuarioSistema.setTipoDeDocumento(tipoDeDocumento);
            String logo = usuarioSistemaDTO.getLogo();
            if (logo != null && !logo.isBlank()) {
                usuarioSistema.setUrlLogo(logo);
            }
            usuarioSistema.setUserName(usuarioSistemaDTO.getUserName());

            usuarioSistema = this.usuarioSistemaRepository.save(usuarioSistema);

            //Solo se debe de crear la clave si el usuario no se va editar, debido a que no se debe de cambiar la contraseña
            if (!usuarioSistemaDTO.getEsEdicion()) {
                Aes aes = new Aes();
                String password = usuarioSistemaDTO.getPassword();
                String passwordEncrypt = aes.encrypt(aesClave, password);

                List<PasswordUserSistema> passwordUserSistemaList = this.passwordUserSistemaRepository.findByUsuarioSistemaIdUsuarioSistemaAndRemovido(
                        usuarioSistema.getIdUsuarioSistema(), false
                );

                for (PasswordUserSistema passwordUserSistema : passwordUserSistemaList) {
                    passwordUserSistema.setFechaEliminacion(new Date());
                    passwordUserSistema.setUsuarioSistemaElimina(usuarioQueCrea);
                    passwordUserSistema.setIpElimina(ipQueCrea);
                    passwordUserSistema.setRemovido(true);
                    this.passwordUserSistemaRepository.save(passwordUserSistema);
                }

                PasswordUserSistema passwordUserSistema = new PasswordUserSistema();
                passwordUserSistema.setPassword(this.passwordEncoder.encode(password));
                passwordUserSistema.setPasswordEncrypt(passwordEncrypt);
                passwordUserSistema.setUsuarioSistemaCrea(usuarioQueCrea);
                passwordUserSistema.setIpCrea(ipQueCrea);
                passwordUserSistema.setUsuarioSistema(usuarioSistema);
                this.passwordUserSistemaRepository.save(passwordUserSistema);
            }

            usuarioSistemaDTO.setTokenIdentificador(usuarioSistema.getTokenIdentificador());

            String accion = "creado";

            if (usuarioSistemaDTO.getEsEdicion()) {
                accion = "editado";
            }
            df.llenarRespuestaExitosa("Se ha " + accion + " con éxito el usuario con nombres: " +
                    usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos() + " y número de documento: "
                    + usuarioSistema.getNumeroDeDocumento() + "(" + usuarioSistema.getTokenIdentificador() + ")", usuarioSistemaDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> crearUsuarioDirecto(HttpServletRequest httpServletRequest, UsuarioSistemaDTO usuarioSistemaDTO) {
        RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            df = this.crearUsuario(usuarioSistemaDTO, null, httpServletRequest.getRemoteAddr());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<CargaMasivaResponse> subirUsuariosCargaMasiva(HttpServletRequest httpServletRequest, CargaMasivaRequest cargaMasivaRequest) {
        RespuestaPorDefectoAuditoria<CargaMasivaResponse> df = new RespuestaPorDefectoAuditoria<>();
        logger.info("Procesando carga masiva de usuarios");

        int totalProcesados = 0;
        int exitosos = 0;
        List<String> errores = new ArrayList<>();

        for (Map.Entry<String, List<UsuarioCargaMasivaRequest>> entry : cargaMasivaRequest.getCargas().entrySet()) {
            String tipoCentro = entry.getKey();
            List<UsuarioCargaMasivaRequest> usuarios = entry.getValue();

            logger.info("Procesando " + usuarios.size() + " usuarios del tipo de centro: " + tipoCentro);

            for (UsuarioCargaMasivaRequest usuarioRequest : usuarios) {
                totalProcesados++;
                try {
                    procesarUsuario(usuarioRequest, tipoCentro);
                    exitosos++;
                } catch (Exception e) {
                    logger.info("Error al procesar usuario: " + usuarioRequest.getNombres() + " " + usuarioRequest.getApellidos() + ", error: " + e.getMessage());
                    errores.add("Error al procesar usuario " + usuarioRequest.getNombres() + " "
                            + usuarioRequest.getApellidos() + ": " + e.getMessage());
                }
            }
        }

        CargaMasivaResponse cresp = CargaMasivaResponse.builder()
                .exito(errores.isEmpty())
                .mensaje(errores.isEmpty() ? "Carga masiva completada exitosamente" : "Carga masiva completada con errores")
                .totalProcesados(totalProcesados)
                .registrosExitosos(exitosos)
                .registrosFallidos(totalProcesados - exitosos)
                .errores(errores)
                .build();

        df.setData(cresp);
        return df;
    }

    @Transactional
    private void procesarUsuario(UsuarioCargaMasivaRequest usuarioRequest, String tipoCentro) {

        List<Catalogo> tipoDocumentoList = catalogoRepository.findByNombreIgnoreCaseAndRemovido(usuarioRequest.getTipoDocumento(), Boolean.FALSE);
        if (tipoDocumentoList == null || tipoDocumentoList.isEmpty()) {
            throw new RuntimeException("No se encontró el tipo de documento: " + usuarioRequest.getTipoDocumento());
        }
        Catalogo tipoDocumento = tipoDocumentoList.get(0);

        List<Jerarquia> departamentoList = jerarquiaRepository.findByNombreIgnoreCaseAndRemovido(usuarioRequest.getDepartamentoCentroSoa(), Boolean.FALSE);
        if (departamentoList == null || departamentoList.isEmpty()) {
            throw new RuntimeException("No se encontró el departamento: " + usuarioRequest.getDepartamentoCentroSoa());
        }
        Jerarquia departamento = departamentoList.get(0);

        List<CargosJerarquia> cargoList = cargosJerarquiaRepository.findByNombreIgnoreCaseAndRemovido(usuarioRequest.getCargo(), Boolean.FALSE);
        if (cargoList == null || cargoList.isEmpty()) {
            throw new RuntimeException("No se encontró el cargo: " + usuarioRequest.getCargo());
        }
        CargosJerarquia cargo = cargoList.get(0);

        List<Rol> rolList = rolRepository.findByNombreIgnoreCaseAndRemovido(usuarioRequest.getRol(), Boolean.FALSE);
        if (rolList == null || rolList.isEmpty()) {
            throw new RuntimeException("No se encontró el rol: " + usuarioRequest.getRol());
        }
        Rol rol = rolList.get(0);

        // OPCION: Parametrizar el parametro con el que se busca la empresa para que se obtenga desde parametros sistema, este parametro puede ser id, nombre-corto, etc
        Empresa empresa = empresaRepository.findByIdEmpresaAndRemovido(1L, Boolean.FALSE);
        if (empresa == null) {
            throw new RuntimeException("No se encontró la empresa con id: 1");
        }

        // 1. CREACION FUNCIONARIO
        FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
        funcionarioDTO.setEsEdicion(Boolean.FALSE);
        funcionarioDTO.setNombres(usuarioRequest.getNombres());
        funcionarioDTO.setApellidos(usuarioRequest.getApellidos());
        funcionarioDTO.setEmail(usuarioRequest.getEmail());
        funcionarioDTO.setTelefono(usuarioRequest.getCelular());
        funcionarioDTO.setNumeroDeCelular(usuarioRequest.getCelular());
        funcionarioDTO.setNumeroDeDocumento(usuarioRequest.getDocumento());
        funcionarioDTO.setTokenIdentificadorTipoDeDocumento(tipoDocumento.getTokenIdentificador());
        funcionarioDTO.setIdDepartamento(departamento.getIdJerarquia());
        funcionarioDTO.setTokenIdentificadorCargo(cargo.getTokenIdentificador());

        RespuestaPorDefectoAuditoria<FuncionarioDTO> respuestaFuncionario = this.funcionarioServiceImpl.crearFuncionarioCargaMasiva(funcionarioDTO);
        if (!respuestaFuncionario.isExito()) {
            throw new RuntimeException("Error al crear funcionario: " + respuestaFuncionario.getMensaje());
        }

        // 2. CREACION USUARIO SISTEMA

        UsuarioSistemaDTO usuarioSistemaDTO = new UsuarioSistemaDTO();
        String passwordTemp = this.funcionesAyuda.crearCadenaAleatoria(8);
        usuarioSistemaDTO.setEsEdicion(Boolean.FALSE);
        usuarioSistemaDTO.setPassword(passwordTemp);

        usuarioSistemaDTO.setNombres(usuarioRequest.getNombres());
        usuarioSistemaDTO.setApellidos(usuarioRequest.getApellidos());
        usuarioSistemaDTO.setEmail(usuarioRequest.getEmail());
        usuarioSistemaDTO.setTelefono(usuarioRequest.getCelular());
        usuarioSistemaDTO.setNumeroDeCelular(usuarioRequest.getCelular());
        usuarioSistemaDTO.setNumeroDeDocumento(usuarioRequest.getDocumento());
        usuarioSistemaDTO.setTokenIdentificadorTipoDeDocumento(tipoDocumento.getTokenIdentificador());
        usuarioSistemaDTO.setUserName(usuarioRequest.getNombreUsuario());

        RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> respuestaUsuario = this.crearUsuario(usuarioSistemaDTO, null, null);
        if (!respuestaUsuario.isExito()) {
            throw new RuntimeException("Error al crear usuario: " + respuestaUsuario.getMensaje());
        }

        UsuarioSistema usuarioSistema = this.usuarioSistemaRepository.findByTokenIdentificadorAndRemovido(
                respuestaUsuario.getData().getTokenIdentificador(), false
        );

        if (usuarioSistema == null) {
            throw new RuntimeException("No se encontró el usuario recien creado");
        }

        usuarioSistema.setEstado(this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(EtiquetaNemonico.CATA_USER_CAMBIO_DE_PASSWORD,
                null, false));

        usuarioSistema = this.usuarioSistemaRepository.save(usuarioSistema);

        // 3. CREACION DE RELACION USUARIO CON ROL Y EMPRESA        
        UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = new UsuarioSistemaEmpresaRol();
        usuarioSistemaEmpresaRol.setUsuarioSistema(usuarioSistema);
        usuarioSistemaEmpresaRol.setEmpresa(empresa);
        usuarioSistemaEmpresaRol.setRol(rol);

        this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);

        // 4. ENVIO POR CORREO DE PASSWORD TEMPORAL PARA USUARIO NUEVO
        Map<String, String> valoresTemplate = new HashMap<>();
        String nombreCompleto = (usuarioSistemaDTO.getNombres() + " " + usuarioSistemaDTO.getApellidos()).trim();
        valoresTemplate.put("[NOMBRE_USUARIO]", nombreCompleto);
        valoresTemplate.put("[NOMBRE_ROL]", rol.getNombre());
        valoresTemplate.put("[USERNAME]", usuarioSistemaDTO.getUserName());
        valoresTemplate.put("[PASSWORD]", passwordTemp);

        List<String> emailsTo = new ArrayList<>();
        emailsTo.add(usuarioSistemaDTO.getEmail());

        RespuestaPorDefectoAuditoria<Boolean> respuestaEnvioCorreo = this.emailService.enviarCorreoConTemplate(
                EtiquetaNemonico.CORREO_CREACION_USUARIO_SOPORTE, emailsTo, empresa.getTokenIdentificador(), valoresTemplate);

        if (!respuestaEnvioCorreo.isExito()) {
            throw new RuntimeException("Error al enviar el email: " + respuestaEnvioCorreo.getMensaje());
        }

    }

    @Override
    public RespuestaPorDefectoAuditoria<UserDataResponse> obtenerDataDelUsuarioLogeado(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<UserDataResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            df.setTokenIdentificadorEmpresa(df2.getTokenIdentificadorEmpresa());

            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();
            UserDataResponse userDataResponse = new UserDataResponse();
            userDataResponse.setId(usuarioSistema.getTokenIdentificador());
            userDataResponse.setName((usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos()).trim());
            userDataResponse.setAvatar(usuarioSistema.getUrlLogo());
            userDataResponse.setEmail(usuarioSistema.getEmail());

            userDataResponse.setRol(df2.getData().getRol().getNombre());
            userDataResponse.setEmpresa(df2.getData().getEmpresa().getNombre());
            userDataResponse.setTelefono(usuarioSistema.getTelefono());
            userDataResponse.setUsername(usuarioSistema.getUserName());

            df.llenarRespuestaExitosa("Datos obtenidos con éxito del usuario con nombre: " +
                    usuarioSistema.getNombres(), userDataResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<UserDataResponse> actualizarDatosDePerfilDelUsuario(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<UserDataResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            ActualizacionDatosUsuarioRequest actualizacionDatosUsuarioRequest = new Gson().fromJson(body, ActualizacionDatosUsuarioRequest.class);

            RespuestaPorDefectoAuditoria<Boolean> df3 = this.recaptchaService.verificarRecaptchaV3(actualizacionDatosUsuarioRequest.getTokenRecaptchaV3(),
                    empresa.getIdEmpresa());

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();

            if (!usuarioSistema.getTokenIdentificador().equals(actualizacionDatosUsuarioRequest.getTokenIdentificador())) {
                df.setMensaje("La identificación no coincide con el de la sesión");
                return df;
            }


            String userNameRequest = actualizacionDatosUsuarioRequest.getUserName();

            if (userNameRequest != null && !userNameRequest.isBlank() && !userNameRequest.isEmpty()) {
                List<UsuarioSistema> usuarioSistemaListUserName = this.usuarioSistemaRepository.findByUserNameAndRemovido(
                        userNameRequest, false
                );

                if (!usuarioSistemaListUserName.isEmpty()) {
                    df.setMensaje("Ya existe un usuario con el username: " + userNameRequest + " envia otro para continuar");
                    return df;
                }

                usuarioSistema.setUserName(userNameRequest);

            }


            String email = actualizacionDatosUsuarioRequest.getEmail();

            if (email != null && !email.isBlank() && !email.isEmpty()) {
                List<UsuarioSistema> usuarioSistemaListEmail = this.usuarioSistemaRepository.findByEmailAndRemovido(email, false);

                if (!usuarioSistemaListEmail.isEmpty()) {
                    df.setMensaje("Ya existe un usuario con el email: " + email + " envia otro para continuar");
                    return df;
                }

                usuarioSistema.setEmail(email);

            }

            String telefono = actualizacionDatosUsuarioRequest.getTelefono();

            if (telefono != null && !telefono.isEmpty() && !telefono.isBlank()) {
                usuarioSistema.setTelefono(telefono);
            }

            usuarioSistema.setUsuarioSistemaEdita(usuarioSistema);
            usuarioSistema.setFechaEdicion(new Date());
            usuarioSistema.setIpElimina(httpServletRequest.getRemoteAddr());

            this.usuarioSistemaRepository.save(usuarioSistema);

            UserDataResponse userDataResponse = new UserDataResponse();
            userDataResponse.setUsername(userNameRequest);
            userDataResponse.setName(usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos());
            userDataResponse.setEmail(usuarioSistema.getEmail());
            userDataResponse.setId(usuarioSistema.getTokenIdentificador());
            userDataResponse.setRol(df2.getData().getRol().getNombre());
            userDataResponse.setAvatar(usuarioSistema.getUrlLogo());
            userDataResponse.setTelefono(usuarioSistema.getTelefono());
            userDataResponse.setEmpresa(empresa.getNombre());


            df.llenarRespuestaExitosa("Se ha actualizado los datos correctamente", userDataResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

}
