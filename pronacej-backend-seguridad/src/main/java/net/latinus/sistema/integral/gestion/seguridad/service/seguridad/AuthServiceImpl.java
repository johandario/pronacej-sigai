package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.PermisoRolUsuario;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.AdministrarMenuRolRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.LoginRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.LoginResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.UserDataResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.security.PasswordEncoder;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private RecaptchaService recaptchaService;
    private MenuRepository menuRepository;
    private MenuEmpresaRolRepository menuEmpresaRolRepository;
    private UsuarioSistemaRepository usuarioSistemaRepository;
    private UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository;
    private JwtProviderService jwtProviderService;
    private PasswordUserSistemaRepository passwordUserSistemaRepository;
    private PasswordEncoder passwordEncoder;
    private ParametroDelSistemaService parametroDelSistemaService;
    private MenuService menuService;
    private UsuarioSistemaService usuarioSistemaService;
    private RolService rolService;
    private MenuEmpresaRolService menuEmpresaRolService;
    private RolRepository rolRepository;
    private CatalogoRepository catalogoRepository;
    private EmailService emailService;
    private PaginacionService paginacionService;

    private ReseteoDePasswordRepository reseteoDePasswordRepository;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private FuncionarioJerarquiaRolRepository funcionarioJerarquiaRolRepository;
    private JerarquiaRepository jerarquiaRepository;

    @Autowired
    public AuthServiceImpl(RecaptchaService recaptchaService,
                           MenuRepository menuRepository, MenuEmpresaRolRepository menuEmpresaRolRepository,
                           UsuarioSistemaRepository usuarioSistemaRepository,
                           UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository,
                           JwtProviderService jwtProviderService, PasswordUserSistemaRepository passwordUserSistemaRepository,
                           PasswordEncoder passwordEncoder, ParametroDelSistemaService parametroDelSistemaService,
                           MenuService menuService, UsuarioSistemaService usuarioSistemaService,
                           RolService rolService, MenuEmpresaRolService menuEmpresaRolService,
                           RolRepository rolRepository, CatalogoRepository catalogoRepository,
                           EmailService emailService,
                           PaginacionService paginacionService, ReseteoDePasswordRepository reseteoDePasswordRepository,
                           ParametroDelSistemaRepository parametroDelSistemaRepository,
                           FuncionarioJerarquiaRolRepository funcionarioJerarquiaRolRepository,
                           JerarquiaRepository jerarquiaRepository) {
        this.recaptchaService = recaptchaService;
        this.menuRepository = menuRepository;
        this.menuEmpresaRolRepository = menuEmpresaRolRepository;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.usuarioSistemaEmpresaRolRepository = usuarioSistemaEmpresaRolRepository;
        this.jwtProviderService = jwtProviderService;
        this.passwordUserSistemaRepository = passwordUserSistemaRepository;
        this.passwordEncoder = passwordEncoder;
        this.parametroDelSistemaService = parametroDelSistemaService;
        this.menuService = menuService;
        this.usuarioSistemaService = usuarioSistemaService;
        this.rolService = rolService;
        this.menuEmpresaRolService = menuEmpresaRolService;
        this.rolRepository = rolRepository;
        this.catalogoRepository = catalogoRepository;
        this.emailService = emailService;
        this.paginacionService = paginacionService;
        this.reseteoDePasswordRepository = reseteoDePasswordRepository;
        this.parametroDelSistemaRepository = parametroDelSistemaRepository;
        this.funcionarioJerarquiaRolRepository = funcionarioJerarquiaRolRepository;
        this.jerarquiaRepository = jerarquiaRepository;
    }

    @Value("${urlFront}")
    private String urlFront;

    private final FuncionesAyuda funcionesAyuda = new FuncionesAyuda();
    private final LogService logService = new LogService(AuthServiceImpl.class);

    @Override
    public RespuestaPorDefectoAuditoria<LoginResponse> loginUserSistema(HttpServletRequest httpServletRequest,
                                                                        BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<LoginResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {
            //RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
            //        null);

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarRSAPorEmpresa(this.parametroDelSistemaRepository);

            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String loginRequestString = df22.getData();

            LoginRequest loginRequest = new Gson().fromJson(loginRequestString, LoginRequest.class);

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.recaptchaService.verificarRecaptchaV3(loginRequest.getRecaptchaV3(), null);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                return df;
            }

            List<UsuarioSistema> usuarioSistemaList = this.usuarioSistemaRepository.findByUserNameAndRemovido(
                    loginRequest.getUserName(), false
            );

            if (usuarioSistemaList.isEmpty()) {
                df.setMensaje("Usuario y/o contraseña no válidos");
                df.setMensajeErrorReal("Usuario y/o contraseña no válidos");
                return df;
            }

            if (usuarioSistemaList.size() > 1) {
                this.logService.warn("Se ha encontrado más de un usuario con el username: " + loginRequest.getUserName());
            }

            UsuarioSistema usuarioSistema = usuarioSistemaList.getFirst();

            List<PasswordUserSistema> passwordUserSistemaList = this.passwordUserSistemaRepository.findByUsuarioSistemaIdUsuarioSistemaAndRemovido(
                    usuarioSistema.getIdUsuarioSistema(), false
            );

            if (passwordUserSistemaList.isEmpty()) {
                df.setMensaje("Usuario y/o contraseña no válidos");
                return df;
            }

            if (passwordUserSistemaList.size() > 1) {
                this.logService.warn("El usuario username: " + loginRequest.getUserName() + " tiene más de una contraseña activa");
            }

            PasswordUserSistema passwordUserSistema = passwordUserSistemaList.get(0);

            if (!this.passwordEncoder.matches(loginRequest.getPassword(), passwordUserSistema.getPassword())) {
                df.setMensaje("Usuario y/o contraseña no válidos");
                return df;
            }

            if (usuarioSistema.getBloqueado()) {
                df.setMensaje("Tu usuario está bloqueado, consulta a tu administrador");
                return df;
            }

            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository
                    .findByUsuarioSistemaIdUsuarioSistemaAndRemovido(usuarioSistema.getIdUsuarioSistema(), false);

            if (usuarioSistemaEmpresaRolList.isEmpty()) {
                df.setMensaje("Tu usuario no tiene asociado un rol y una empresa, comunicate con tu administrador");
                return df;
            }

            UsuarioSistemaEmpresaRol elegido = usuarioSistemaEmpresaRolList.get(0);

            usuarioSistemaEmpresaRolList = Collections.singletonList(elegido);

            //Caso deseado
            if (usuarioSistemaEmpresaRolList.size() == 1) {

                UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = usuarioSistemaEmpresaRolList.get(0);
//                Rol rolUser = usuarioSistemaEmpresaRol.getRol();

//                if (rolUser.getBloqueado()) {
//                    df.setMensaje("Tu rol esta bloqueado para el uso, comunicate con tu administrador");
//                    return df;
//                }

                Empresa empresa = usuarioSistemaEmpresaRol.getEmpresa();

                if (empresa.getBloqueado()) {
                    df.setMensaje("Tu empresa esta bloqueda para el uso, comunicate con tu administrador");
                    return df;
                }

                df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

                BodyJWTFront bodyJWTFront = new BodyJWTFront();
                LoginResponse loginResponse = new LoginResponse();
//                loginResponse.setNombreRol(rolUser.getNombre());
                loginResponse.setNombreEmpresa(empresa.getNombre());
                loginResponse.setEstado(EtiquetaNemonico.LOGIN_EXITO);

                UserDataResponse userDataResponse = new UserDataResponse();

                //Verificando si la contraseña esta expirada
                String nemonico = EtiquetaNemonico.PARAM_REGLA_CONTRASENIA_CAMBIO_CADA_N_DIAS;
                if (empresa.getUsuariosDebenDeCambiarContraseniaLuegoDeNDias() != null && empresa.getUsuariosDebenDeCambiarContraseniaLuegoDeNDias()) {
                    ParametroDelSistema parametroDelSistema = this.parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                            nemonico,
                            null
                    );

                    Integer maxDias;
                    if (parametroDelSistema == null) {
                        this.logService.warn("No se ha establecido un parametro del sistema con la cantidad de dias de expiración de contraseña ,"
                                + " con nemonico: " + nemonico);
                        maxDias = 90;
                    } else {
                        String valor = parametroDelSistema.getValor();

                        if (valor == null || valor.isEmpty() || valor.isBlank()) {
                            this.logService.warn("El valor del parametro de sistema con nemonico: " +
                                    nemonico + " es vacio o nulo");
                            maxDias = 90;
                        } else {
                            maxDias = Integer.valueOf(valor);
                        }
                    }

                    Date fechaContrasenia = passwordUserSistema.getFechaCreacion();
                    if (fechaContrasenia == null) {
                        fechaContrasenia = new Date();
                        passwordUserSistema.setFechaHabilitada(fechaContrasenia);
                        this.passwordUserSistemaRepository.save(passwordUserSistema);
                    }

                    Date fechaActual = new Date();
                    Long dateTimeContrasenia = fechaContrasenia.getTime();
                    Long dateTimeActual = fechaActual.getTime();

                    Long diferenciaMilis = dateTimeActual - dateTimeContrasenia;
                    Long diasDiferencia = (((diferenciaMilis / 1000) / 60) / 60) / 24;
                    this.logService.info("Se verifica");
                    if (diasDiferencia > maxDias) {
                        loginResponse.setEstado(EtiquetaNemonico.LOGIN_CAMBIO_DE_CONTRASENIA);

                        ReseteoDePassword reseteoDePassword = new ReseteoDePassword();
                        reseteoDePassword.setIpCrea(httpServletRequest.getRemoteAddr());
                        reseteoDePassword.setEmpresa(empresa);
                        reseteoDePassword.setUsuarioSistemaCrea(usuarioSistema);
                        reseteoDePassword.setEstado(
                                this.catalogoRepository.findByNemonicoAndRemovido(
                                        EtiquetaNemonico.NEMONICO_RESETEO_DE_PASSWORD_PENDIENTE,
                                        false
                                )
                        );
                        reseteoDePassword.setUsuarioSistemaEmpresaRol(usuarioSistemaEmpresaRol);
                        this.reseteoDePasswordRepository.save(reseteoDePassword);
                        userDataResponse.setTokenReseteoContrasenia(reseteoDePassword.getTokenIdentificador());
                    }
                }

                loginResponse.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
                //loginResponse.setMenu(menuDTOList);

                userDataResponse.setId(usuarioSistema.getTokenIdentificador());
                userDataResponse.setName((usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos()).trim());
                userDataResponse.setAvatar(usuarioSistema.getUrlLogo());
                userDataResponse.setEmail(usuarioSistema.getEmail());

                loginResponse.setUserDataResponse(userDataResponse);

                List<FuncionarioJerarquiaRol> fjrList = funcionarioJerarquiaRolRepository
                        .findByFuncionario_TokenIdentificadorAndRemovidoFalseAndRolIsNotNull(
                                usuarioSistema.getFuncionario().getTokenIdentificador()
                        );

                if (fjrList.isEmpty()) {
                    df.setMensaje("Tu usuario no tiene asignada ninguna jerarquía, comunícate con tu administrador");
                    return df;
                }

                if (loginRequest.getTokenIdentificadorJerarquia() != null) {
                    Optional<FuncionarioJerarquiaRol> seleccion = fjrList.stream()
                            .filter(fjr -> loginRequest.getTokenIdentificadorJerarquia()
                                    .equals(fjr.getJerarquia().getTokenIdentificador()))
                            .findFirst();

                    if (seleccion.isPresent()) {
                        FuncionarioJerarquiaRol asig = seleccion.get();
                        Jerarquia jer = asig.getJerarquia();
                        Rol rol = asig.getRol();  // puede ser null si opcional

                        // valida rol si existe
                        if (rol != null && rol.getBloqueado()) {
                            df.setMensaje("El rol de la jerarquía seleccionada está bloqueado");
                            return df;
                        }

                        RespuestaPorDefectoAuditoria<List<MenuDTO>> df4 = this.crearMenuPorRolYEmpresa(rol, empresa, false);
                        if (!df4.isExito()) {
                            df.setMensaje(df4.getMensaje());
                            return df;
                        }

                        List<MenuDTO> menuDTOList = df4.getData();

                        if (menuDTOList.isEmpty()) {
                            df.setMensaje("No tienes configurado los menu, comunicate con tu administrador");
                            return df;
                        }

                        loginResponse.setTokenIdentificadorJerarquia(jer.getTokenIdentificador());
                        loginResponse.setNombreRol(rol != null ? rol.getNombre() : "");
                        loginResponse.setTokenIdentificadorRolJerarquia(rol != null ? rol.getTokenIdentificador() : "");

                        bodyJWTFront.setIdentificadorRolJerarquia(rol != null ? rol.getTokenIdentificador() : "");
                        bodyJWTFront.setIdentificadorJerarquia(jer.getTokenIdentificador());


                    } else {
                        df.setMensaje("La jerarquía seleccionada no es válida para este usuario");
                        return df;
                    }
                } else if (fjrList.size() == 1) {
                    FuncionarioJerarquiaRol asig = fjrList.get(0);
                    Jerarquia jer = asig.getJerarquia();
                    Rol rol = asig.getRol();

                    if (rol != null && rol.getBloqueado()) {
                        df.setMensaje("El rol de tu única jerarquía está bloqueado");
                        return df;
                    }

                    RespuestaPorDefectoAuditoria<List<MenuDTO>> df4 = this.crearMenuPorRolYEmpresa(rol, empresa, false);
                    if (!df4.isExito()) {
                        df.setMensaje(df4.getMensaje());
                        return df;
                    }

                    List<MenuDTO> menuDTOList = df4.getData();

                    if (menuDTOList.isEmpty()) {
                        df.setMensaje("No tienes configurado los menu, comunicate con tu administrador");
                        return df;
                    }

                    loginResponse.setTokenIdentificadorJerarquia(jer.getTokenIdentificador());
                    loginResponse.setNombreRol(rol != null ? rol.getNombre() : "");
                    loginResponse.setTokenIdentificadorRolJerarquia(rol != null ? rol.getTokenIdentificador() : "");
                    bodyJWTFront.setIdentificadorRolJerarquia(rol != null ? rol.getTokenIdentificador() : "");
                    bodyJWTFront.setIdentificadorRol(rol != null ? rol.getTokenIdentificador() : "");
                    bodyJWTFront.setIdentificadorJerarquia(jer.getTokenIdentificador());

                    // 5) Si hubiera varias y no vino token en el payload, devolvemos un estado de selección de jerarquía
                } else {
                    LoginResponse resp = new LoginResponse();
                    resp.setEstado(EtiquetaNemonico.LOGIN_SELECCION_DE_JERARQUIA);
                    List<JerarquiaDTO> opciones = fjrList.stream().map(fjr -> {
                        JerarquiaDTO dto = new JerarquiaDTO();
                        dto.setTokenIdentificador(fjr.getJerarquia().getTokenIdentificador());
                        dto.setNombre(fjr.getJerarquia().getNombre());
                        return dto;
                    }).collect(Collectors.toList());
                    resp.setListaJerarquias(opciones);
                    df.llenarRespuestaExitosa("Selecciona la jerarquía con la que deseas ingresar", resp);
                    return df;
                }


                bodyJWTFront.setIdentificadorEmpresa(empresa.getTokenIdentificador());
                bodyJWTFront.setIdentificadorUsuarioSistema(usuarioSistema.getTokenIdentificador());
//                bodyJWTFront.setIdentificadorRol(rolUser.getTokenIdentificador());

                RespuestaPorDefectoAuditoria<String> df3 = this.jwtProviderService.crearJwt(bodyJWTFront.toString(),
                        empresa.getIdEmpresa(),
                        this.parametroDelSistemaService);
                if (!df3.isExito()) {
                    df.setMensaje(df3.getMensaje());
                    return df;
                }
                loginResponse.setJwt(df3.getData());


                df.llenarRespuestaExitosa("Login exitoso",
                        loginResponse);
                return df;
            }

            //Caso varias empresas
            Set<Empresa> empresas = usuarioSistemaEmpresaRolList.stream().map((uSeR) -> uSeR.getEmpresa()).collect(Collectors.toSet());
            if (empresas.size() > 1) {
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setEstado(EtiquetaNemonico.LOGIN_SELECCION_DE_EMPRESA);
                RespuestaPorDefectoAuditoria<String> df3 = this.jwtProviderService.crearJwt(usuarioSistema.getTokenIdentificador(), null
                        , this.parametroDelSistemaService);

                if (!df3.isExito()) {
                    df.setMensaje(df3.getMensaje());
                    return df;
                }

                loginResponse.setJwt(df3.getData());
                df.llenarRespuestaExitosa("Login exitoso para seleccionar empresa", loginResponse);
            }

            Set<Rol> roles = usuarioSistemaEmpresaRolList.stream().map((uSeR) -> uSeR.getRol()).collect(Collectors.toSet());
            if (roles.isEmpty()) {
                df.setMensaje("Tu usuario no tiene asociado un rol");
                return df;
            }

            df.setMensaje("Tu usuario no tiene una configuración especifica, comunicate con tu administrador");


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> crearMenuPorRolYEmpresa(Rol rol, Empresa empresa, Boolean esCompact) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // TODO CAMBIAR LOGICA DE MENUS POR ROLES
            List<Menu> menuAsignados = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(true,
                    null, false);

            /*
            if (rol.getEsSuperRol()) {
                menuAsignados = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovido(true, false, false);
            } else {
                List<MenuEmpresaRol> menuEmpresaRolList = this.menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndRemovido(
                        empresa.getIdEmpresa(), rol.getIdRol(), false
                );
                menuAsignados = new ArrayList<>();
                for (MenuEmpresaRol menuEmpresaRol : menuEmpresaRolList) {
                    menuAsignados.add(menuEmpresaRol.getMenu());
                }
            }
            */

            List<MenuDTO> menuDTOList = this.menuService.obtenerMenusDeMenusPadres(menuAsignados, esCompact, true, rol, empresa);
            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    menuDTOList.size() + " menus principales", menuDTOList);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<LoginResponse> verificarJwt(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<LoginResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Rol rolUser = bodyJwtValido.getRol();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(df2.getTokenIdentificadorEmpresa());

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setJwt(bodyJwtValido.getJwt());
            loginResponse.setNombreRol(rolUser.getNombre());
            loginResponse.setNombreEmpresa(empresa.getNombre());
            loginResponse.setEstado(EtiquetaNemonico.LOGIN_EXITO);
            loginResponse.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            //loginResponse.setMenu(menuDTOList);

            UserDataResponse userDataResponse = new UserDataResponse();
            userDataResponse.setId(usuarioSistema.getTokenIdentificador());
            userDataResponse.setName((usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos()).trim());
            userDataResponse.setAvatar(usuarioSistema.getUrlLogo());
            userDataResponse.setEmail(usuarioSistema.getEmail());
            loginResponse.setUserDataResponse(userDataResponse);

            df.llenarRespuestaExitosa("Jwt válidado con éxito", loginResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<CreacionDeUsuarioSistema> creaUnUsuarioDelSistema(HttpServletRequest httpServletRequest,
                                                                                          BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<CreacionDeUsuarioSistema> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            CreacionDeUsuarioSistema creacionDeUsuarioSistema = new Gson().fromJson(bodyString, CreacionDeUsuarioSistema.class);

//            Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(
//                    creacionDeUsuarioSistema.getTokenIdentificadorRol(), false
//            );

//            if (rol == null) {
//                df.setMensaje("El rol no existe o fue eliminado anteriormente");
//                return df;
//            }

//            if (rol.getBloqueado()) {
//                df.setMensaje("El rol esta bloqueado para el uso, elige otro para continuar");
//                return df;
//            }

            List<UsuarioSistema> usersTemp = this.usuarioSistemaRepository.findByUserNameAndRemovido(
                    creacionDeUsuarioSistema.getUserName(), false
            );
            if (!usersTemp.isEmpty() && !creacionDeUsuarioSistema.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el username: " + creacionDeUsuarioSistema.getUserName() + " envia otro para continuar");
                return df;
            }

            List<UsuarioSistema> usersTempEmail = this.usuarioSistemaRepository.findByEmailAndRemovido(
                    creacionDeUsuarioSistema.getEmail(), false
            );
            if (!usersTempEmail.isEmpty() && !creacionDeUsuarioSistema.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el email: " + creacionDeUsuarioSistema.getEmail() + " envia otro para continuar");
                return df;
            }

            UsuarioSistema userTempDocumento = this.usuarioSistemaRepository.findByNumeroDeDocumentoAndRemovido(
                    creacionDeUsuarioSistema.getNumeroDeDocumento(), false);

            if (userTempDocumento != null && !creacionDeUsuarioSistema.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el número de documento: " + creacionDeUsuarioSistema.getNumeroDeDocumento() + " envia otro para continuar");
                return df;
            }

            UsuarioSistemaDTO usuarioSistemaDTO = creacionDeUsuarioSistema.obtenerUsuarioSistemaDTO();
            String passwordTemp = this.funcionesAyuda.crearCadenaAleatoria(8);
            usuarioSistemaDTO.setPassword(passwordTemp);

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            //Enviando correo
            if (!creacionDeUsuarioSistema.getEsEdicion()) {
                Map<String, String> valoresTemplate = new HashMap<>();
                String nombreCompleto = (usuarioSistemaDTO.getNombres() + " " + usuarioSistemaDTO.getApellidos()).trim();
                valoresTemplate.put("[NOMBRE_USUARIO]", nombreCompleto);
//                valoresTemplate.put("[NOMBRE_ROL]", rol.getNombre());
                valoresTemplate.put("[USERNAME]", usuarioSistemaDTO.getUserName());
                valoresTemplate.put("[PASSWORD]", passwordTemp);
                valoresTemplate.put("[URL_PAGINA_ADMINISTRACION]", urlFront);

                List<String> emailsTo = new ArrayList<>();
                emailsTo.add(usuarioSistemaDTO.getEmail());

                RespuestaPorDefectoAuditoria<Boolean> df4 = this.emailService.enviarCorreoConTemplate(
                        EtiquetaNemonico.CORREO_CREACION_USUARIO_SOPORTE, emailsTo, empresa.getTokenIdentificador(), valoresTemplate);

                if (!df4.isExito()) {
                    df.setMensaje(df4.getMensaje());
                    return df;
                }
            }

            RespuestaPorDefectoAuditoria<UsuarioSistemaDTO> df3 = this.usuarioSistemaService.crearUsuario(usuarioSistemaDTO,
                    usuarioLogin, ip);

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                return df;
            }

            UsuarioSistema usuarioSistema = this.usuarioSistemaRepository.findByTokenIdentificadorAndRemovido(
                    df3.getData().getTokenIdentificador(), false
            );

            if (usuarioSistema == null) {
                df.setMensaje("No se pudo crear al usuario correctamente, consulta a tu administrador");
                return df;
            }

            usuarioSistema.setEstado(this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(EtiquetaNemonico.CATA_USER_CAMBIO_DE_PASSWORD,
                    null, false));

            usuarioSistema = this.usuarioSistemaRepository.save(usuarioSistema);

            //Creando la asociacion con el rol
            if (creacionDeUsuarioSistema.getEsEdicion()) {
                List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository.findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRemovido(
                        df2.getData().getEmpresa().getTokenIdentificador(), creacionDeUsuarioSistema.getTokenIdentificador(), false);

                for (UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol : usuarioSistemaEmpresaRolList) {
                    usuarioSistemaEmpresaRol.setRemovido(true);
                    usuarioSistemaEmpresaRol.setFechaEliminacion(new Date());
                    usuarioSistemaEmpresaRol.setIpElimina(ip);
                    usuarioSistemaEmpresaRol.setUsuarioSistemaElimina(usuarioLogin);
                    this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);
                }
            }

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = new UsuarioSistemaEmpresaRol();
            usuarioSistemaEmpresaRol.setUsuarioSistema(usuarioSistema);
            usuarioSistemaEmpresaRol.setUsuarioSistemaCrea(usuarioLogin);
            usuarioSistemaEmpresaRol.setIpCrea(ip);
            usuarioSistemaEmpresaRol.setEmpresa(empresa);
//            usuarioSistemaEmpresaRol.setRol(rol);

            creacionDeUsuarioSistema.setTokenIdentificador(usuarioSistema.getTokenIdentificador());

            this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);

            Funcionario funcionario = usuarioSistema.getFuncionario();
            if (funcionario != null) {
                this.persistirAsignaciones(funcionario, creacionDeUsuarioSistema.getAsignaciones());
            }

            // Obtener nombres completos y datos para el mensaje
            String nombresCompletos = obtenerNombresCompletosUsuario(creacionDeUsuarioSistema);
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Determinar acción
            String accion = creacionDeUsuarioSistema.getEsEdicion() ? "editó" : "creó";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se " + accion + " con exito al usuario con nombre: " + creacionDeUsuarioSistema.getNombres();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accion + " con éxito el usuario " + nombresCompletos +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, creacionDeUsuarioSistema, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<CreacionDeRol> creaUnRol(HttpServletRequest httpServletRequest,
                                                                 BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<CreacionDeRol> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();
            Empresa empresa = df2.getData().getEmpresa();

            CreacionDeRol creacionDeRol = new Gson().fromJson(bodyString, CreacionDeRol.class);

            creacionDeRol.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<RolDTO> df3 = this.rolService.crearOEditarRol(creacionDeRol.obtenerRolDTO(), usuarioLogin, ip);

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                return df;
            }

            Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(
                    df3.getData().getTokenIdentificador(), false
            );

            if (rol == null) {
                df.setMensaje("No se pudo crear al rol correctamente, consulta a tu administrador");
                return df;
            }

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String accion = creacionDeRol.getEsEdicion() ? "editó" : "creó";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se " + accion + " con exito al rol con nombre: " + creacionDeRol.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accion + " con éxito el rol " + creacionDeRol.getNombre() +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, creacionDeRol, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> obtenerUsuarioDelSistema(HttpServletRequest httpServletRequest,
                                                                                                               BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistemaLogin = bodyJwtValido.getUsuarioSistema();
            Empresa empresa = bodyJwtValido.getEmpresa();

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository.
                    findByEmpresaIdEmpresaAndRemovido(
                            empresa.getIdEmpresa(), false);

            PaginacionResponse<CreacionDeUsuarioSistema> paginacionResponse = new PaginacionResponse<>();
            List<CreacionDeUsuarioSistema> creacionDeUsuarioSistemaList = new ArrayList<>();
            for (UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol : usuarioSistemaEmpresaRolList) {
//                Rol rol = usuarioSistemaEmpresaRol.getRol();
                Empresa empresa1 = usuarioSistemaEmpresaRol.getEmpresa();
                UsuarioSistema usuarioSistema = usuarioSistemaEmpresaRol.getUsuarioSistema();

                CreacionDeUsuarioSistema creacionDeUsuarioSistema = new CreacionDeUsuarioSistema();
//                creacionDeUsuarioSistema.setTokenIdentificadorRol(rol.getTokenIdentificador());
//                creacionDeUsuarioSistema.setNombreRol(rol.getNombre());
                creacionDeUsuarioSistema.setEmail(usuarioSistema.getEmail());
                creacionDeUsuarioSistema.setTokenIdentificador(usuarioSistema.getTokenIdentificador());
                creacionDeUsuarioSistema.setTokenIdentificadorEmpresa(empresa1.getTokenIdentificador());
                creacionDeUsuarioSistema.setApellidos(usuarioSistema.getApellidos());
                creacionDeUsuarioSistema.setLogo(usuarioSistema.getUrlLogo());
                creacionDeUsuarioSistema.setNombres(usuarioSistema.getNombres());
                creacionDeUsuarioSistema.setNumeroDeCelular(usuarioSistema.getNumeroDeCelular());
                creacionDeUsuarioSistema.setNumeroDeDocumento(usuarioSistema.getNumeroDeDocumento());
                creacionDeUsuarioSistema.setTelefono(usuarioSistema.getTelefono());
                creacionDeUsuarioSistema.setUserName(usuarioSistema.getUserName());
                creacionDeUsuarioSistema.setFechaCreacion(usuarioSistema.getFechaCreacion());
                creacionDeUsuarioSistema.setTokenRelacion(usuarioSistemaEmpresaRol.getTokenIdentificador());
                creacionDeUsuarioSistema.setBloqueadoRelacion(usuarioSistemaEmpresaRol.getBloqueado());
                if (usuarioSistema.getTipoDeDocumento() != null) {
                    creacionDeUsuarioSistema.setTokenIdentificadorTipoDeDocumento(usuarioSistema.getTipoDeDocumento().getTokenIdentificador());
                }

                Funcionario funcionario = usuarioSistema.getFuncionario();
                if (funcionario != null) {
                    // 2) Trae sus asignaciones activas (jerarquía+rol)
                    List<FuncionarioJerarquiaRol> asigns = funcionarioJerarquiaRolRepository
                            .findByFuncionario_TokenIdentificadorAndRemovidoFalse(
                                    funcionario.getTokenIdentificador()
                            );

                    // 3) Mapea a DTOs y ponlo en el objeto de salida
                    List<FuncionarioJerarquiaRolDTO> asignacionesDto = asigns.stream()
                            .map(a -> {
                                FuncionarioJerarquiaRolDTO dto = new FuncionarioJerarquiaRolDTO();
                                dto.setTokenIdentificadorJerarquia(a.getJerarquia().getTokenIdentificador());
                                dto.setJerarquia(a.getJerarquia().getNombre());
                                if (!ObjectUtils.isEmpty(a.getRol())) {
                                    dto.setTokenIdentificadorRol(a.getRol().getTokenIdentificador());
                                    dto.setRol(a.getRol().getNombre());
                                }
                                return dto;
                            })
                            .collect(Collectors.toList());

                    creacionDeUsuarioSistema.setAsignaciones(asignacionesDto);
                    if (!asigns.isEmpty()) {
                        FuncionarioJerarquiaRol funcionarioJerarquiaRol = asigns.getFirst();
                        if (funcionarioJerarquiaRol.getRol() != null)
                            creacionDeUsuarioSistema.setNombreRol(funcionarioJerarquiaRol.getRol().getNombre());
                        creacionDeUsuarioSistema.setTokenIdentificadorRol(funcionarioJerarquiaRol.getTokenIdentificador());
                    }

                }

                creacionDeUsuarioSistemaList.add(creacionDeUsuarioSistema);
            }

            creacionDeUsuarioSistemaList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);
            paginacionResponse = paginacionService.obtenerDatos(creacionDeUsuarioSistemaList, paginacionRequest);

            // CORREGIDO: Usar el total de elementos en lugar del tamaño de la paginación actual
            long totalElementos = creacionDeUsuarioSistemaList.size(); // Total de usuarios en el sistema
            long elementosPaginaActual = paginacionResponse.getData().size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + totalElementos
                    + " usuarios disponibles en el sistema, mostrando " + elementosPaginaActual + " en esta página. Consulta realizada por: " +
                    usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                    + "(" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " usuarios del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> obtenerUsuarioValidosDelSistema(HttpServletRequest httpServletRequest,
                                                                                                                      BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistemaLogin = bodyJwtValido.getUsuarioSistema();
            Empresa empresa = bodyJwtValido.getEmpresa();

            Rol rolJerarquia = bodyJwtValido.getRolJerarquia();

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();


            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository.
                    findByEmpresaIdEmpresaAndRemovidoAndUsuarioSistemaRemovido(
                            empresa.getIdEmpresa(), false, false);

            PaginacionResponse<CreacionDeUsuarioSistema> paginacionResponse = new PaginacionResponse<>();
            List<CreacionDeUsuarioSistema> creacionDeUsuarioSistemaList = new ArrayList<>();
            for (UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol : usuarioSistemaEmpresaRolList) {
                Rol rol = usuarioSistemaEmpresaRol.getRol();
                Empresa empresa1 = usuarioSistemaEmpresaRol.getEmpresa();
                UsuarioSistema usuarioSistema = usuarioSistemaEmpresaRol.getUsuarioSistema();

                CreacionDeUsuarioSistema creacionDeUsuarioSistema = new CreacionDeUsuarioSistema();
                creacionDeUsuarioSistema.setTokenIdentificadorRol(rolJerarquia.getTokenIdentificador());
                creacionDeUsuarioSistema.setNombreRol(rolJerarquia.getNombre());
                creacionDeUsuarioSistema.setEmail(usuarioSistema.getEmail());
                creacionDeUsuarioSistema.setTokenIdentificador(usuarioSistema.getTokenIdentificador());
                creacionDeUsuarioSistema.setTokenIdentificadorEmpresa(empresa1.getTokenIdentificador());
                creacionDeUsuarioSistema.setApellidos(usuarioSistema.getApellidos());
                creacionDeUsuarioSistema.setLogo(usuarioSistema.getUrlLogo());
                creacionDeUsuarioSistema.setNombres(usuarioSistema.getNombres());
                creacionDeUsuarioSistema.setNumeroDeCelular(usuarioSistema.getNumeroDeCelular());
                creacionDeUsuarioSistema.setNumeroDeDocumento(usuarioSistema.getNumeroDeDocumento());
                creacionDeUsuarioSistema.setTelefono(usuarioSistema.getTelefono());
                creacionDeUsuarioSistema.setUserName(usuarioSistema.getUserName());
                creacionDeUsuarioSistema.setFechaCreacion(usuarioSistema.getFechaCreacion());
                creacionDeUsuarioSistema.setTokenRelacion(usuarioSistemaEmpresaRol.getTokenIdentificador());
                creacionDeUsuarioSistema.setBloqueadoRelacion(usuarioSistemaEmpresaRol.getBloqueado());
                if (usuarioSistema.getTipoDeDocumento() != null) {
                    creacionDeUsuarioSistema.setTokenIdentificadorTipoDeDocumento(usuarioSistema.getTipoDeDocumento().getTokenIdentificador());
                }

                creacionDeUsuarioSistemaList.add(creacionDeUsuarioSistema);
            }

            creacionDeUsuarioSistemaList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);
            paginacionResponse = paginacionService.obtenerDatos(creacionDeUsuarioSistemaList, paginacionRequest);

            // CORREGIDO: Usar el total de elementos en lugar del tamaño de la paginación actual
            long totalElementos = creacionDeUsuarioSistemaList.size(); // Total de usuarios activos en el sistema
            long elementosPaginaActual = paginacionResponse.getData().size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + totalElementos
                    + " usuarios activos disponibles en el sistema, mostrando " + elementosPaginaActual + " en esta página. Consulta realizada por: " +
                    usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                    + "(" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " usuarios activos del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> obtenerRoles(HttpServletRequest httpServletRequest,
                                                                                        BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idRol").descending()
            );

            Page<Rol> rolPage = this.rolRepository.findByEmpresaIdEmpresaAndRemovido(
                    empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<CreacionDeRol> paginacionResponse = new PaginacionResponse<>();
            List<CreacionDeRol> creacionDeRolList = new ArrayList<>();
            for (Rol rol : rolPage.toList()) {
                CreacionDeRol creacionDeRol = new CreacionDeRol();
                creacionDeRol.setTokenIdentificador(rol.getTokenIdentificador());
                creacionDeRol.setTokenIdentificadorEmpresa(rol.getEmpresa().getTokenIdentificador());
                creacionDeRol.setNombre(rol.getNombre());
                creacionDeRol.setCodigo(rol.getCodigo());
                creacionDeRol.setDescripcion(rol.getDescripcion());
                creacionDeRol.setEsSuperRol(rol.getEsSuperRol());
                creacionDeRol.setEsRolPorDefecto(rol.getEsRolPorDefecto());
                creacionDeRol.setDiasExpiracionPassword(rol.getDiasExpiracionPassword());
                creacionDeRol.setBloqueadoRelacion(rol.getBloqueado());

                creacionDeRolList.add(creacionDeRol);
            }

            paginacionResponse.setData(creacionDeRolList);
            paginacionResponse.setTotalItems((rolPage.getTotalElements()));

            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = rolPage.getTotalElements(); // Total de roles en el sistema
            long elementosPaginaActual = creacionDeRolList.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + totalElementos + " roles disponibles en el sistema, mostrando " + elementosPaginaActual + " en esta página";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " roles del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarUsuario(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            CreacionDeUsuarioSistema creacionDeUsuarioSistema = new Gson().fromJson(bodyString, CreacionDeUsuarioSistema.class);

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = this.usuarioSistemaEmpresaRolRepository.findByTokenIdentificadorAndRemovido(
                    creacionDeUsuarioSistema.getTokenRelacion(), false);

            if (usuarioSistemaEmpresaRol == null) {
                df.setMensaje("No se encontro un usuario válido a eliminar o este ya fue eliminado anteriormente");
                df.setMensajeErrorReal("usuarioSistemaEmpresaRol nulo");
                return df;
            }

            if (!empresa.getTokenIdentificador().equals(
                    usuarioSistemaEmpresaRol.getEmpresa().getTokenIdentificador()
            )) {
                df.setMensaje("No puedes eliminar aun usuario de una empresa diferente");
                return df;
            }

            usuarioSistemaEmpresaRol.setRemovido(true);
            usuarioSistemaEmpresaRol.setIpElimina(ip);
            usuarioSistemaEmpresaRol.setUsuarioSistemaElimina(usuarioSistemaLogin);
            usuarioSistemaEmpresaRol.setFechaEliminacion(new Date());
            this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);

            // Obtener datos para el mensaje
            String nombresCompletos = obtenerNombresCompletosUsuario(creacionDeUsuarioSistema);
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se ha eliminado con exito del sistema al usuario: " + creacionDeUsuarioSistema.getUserName();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito el usuario " + nombresCompletos +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            CreacionDeRol creacionDeRol = new Gson().fromJson(bodyString, CreacionDeRol.class);

            Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(
                    creacionDeRol.getTokenIdentificador(), false
            );

            if (rol == null) {
                df.setMensaje("El rol no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Date fecha = new Date();
            rol.setRemovido(true);
            rol.setIpElimina(ip);
            rol.setUsuarioSistemaElimina(usuarioSistemaLogin);
            rol.setFechaEliminacion(fecha);

            this.rolRepository.save(rol);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String fechaFormateada = formatearFechaEspanol(fecha);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se ha eliminado con exito del sistema al rol: " + rol.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito el rol " + rol.getNombre() +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> bloquearUsuarioSistema(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            CreacionDeUsuarioSistema creacionDeUsuarioSistema = new Gson().fromJson(bodyString, CreacionDeUsuarioSistema.class);

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = this.usuarioSistemaEmpresaRolRepository.findByTokenIdentificadorAndRemovido(
                    creacionDeUsuarioSistema.getTokenRelacion(), false
            );

            if (usuarioSistemaEmpresaRol == null) {
                df.setMensaje("La relación no existe o ya fue bloqueada anteriormente");
                return df;
            }

            usuarioSistemaEmpresaRol.setBloqueado(creacionDeUsuarioSistema.getBloqueadoRelacion());

            this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);

            // Obtener datos para el mensaje
            String nombresCompletos = obtenerNombresCompletosUsuario(creacionDeUsuarioSistema);
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String accionBloqueo = creacionDeUsuarioSistema.getBloqueadoRelacion() ? "bloqueó" : "desbloqueó";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "El usuario fue \"" + (creacionDeUsuarioSistema.getBloqueadoRelacion() ? "bloqueado" : "desbloqueado") + "\" con éxito";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accionBloqueo + " con éxito el usuario " + nombresCompletos +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> bloquearRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            CreacionDeRol creacionDeRol = new Gson().fromJson(bodyString, CreacionDeRol.class);

            Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(
                    creacionDeRol.getTokenIdentificador(), false
            );

            if (rol == null) {
                df.setMensaje("El rol no fue encontrado o ya fue bloqueado anteriormente");
                return df;
            }

            Date fecha = new Date();
            rol.setBloqueado(creacionDeRol.getBloqueadoRelacion());

            this.rolRepository.save(rol);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String fechaFormateada = formatearFechaEspanol(fecha);
            String accionBloqueo = creacionDeRol.getBloqueadoRelacion() ? "bloqueó" : "desbloqueó";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "El rol fue \"" + (creacionDeRol.getBloqueadoRelacion() ? "bloqueado" : "desbloqueado") + "\" con éxito";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accionBloqueo + " con éxito el rol " + rol.getNombre() +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearRelacionMenusRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            AdministrarMenuRolRequest request = new Gson().fromJson(bodyString, AdministrarMenuRolRequest.class);
            CreacionDeRol rolRequest = request.getRol();
            List<MenuDTO> listaMenusSeleccionados = request.getListaMenus();
            Rol rol = rolRepository.findByTokenIdentificadorAndRemovido(rolRequest.getTokenIdentificador(), Boolean.FALSE);

            RespuestaPorDefectoAuditoria<List<MenuEmpresaRolDTO>> df3 = this.menuEmpresaRolService.obtenerTodosPorEmpresaYRol(empresa, rol);
            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                return df;
            }
            List<MenuEmpresaRolDTO> listaPermisosActuales = df3.getData();
            if (!listaPermisosActuales.isEmpty()) {
                for (MenuEmpresaRolDTO merDTO : listaPermisosActuales) {
                    MenuEmpresaRol mer = menuEmpresaRolRepository.findByTokenIdentificador(merDTO.getTokenIdentificador());
                    menuEmpresaRolRepository.delete(mer);
                }
            }
            if (!listaMenusSeleccionados.isEmpty()) {
                for (MenuDTO menuDTO : listaMenusSeleccionados) {
                    Menu menu = menuRepository.findByTokenIdentificadorAndRemovido(menuDTO.getTokenIdentificador(), Boolean.FALSE);
                    menuEmpresaRolService.crearMenuEmpresaRol(empresa, rol, menu, usuarioSistemaLogin, ip);
                }
            }

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Los permisos a las pantallas seleccionadas para el rol se guardaron con éxito";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se configuraron con éxito los permisos de menús para el rol " + rol.getNombre() +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, Boolean.TRUE, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusAccesiblesPorRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            CreacionDeRol rolRequest = new Gson().fromJson(bodyString, CreacionDeRol.class);

            Rol rol = rolRepository.findByTokenIdentificadorAndRemovido(rolRequest.getTokenIdentificador(), Boolean.FALSE);

            df = this.menuService.obtenerMenusAccesiblesPorRol(empresa.getIdEmpresa(), rol);

            if (df.isExito()) {
                // Obtener los datos existentes
                List<MenuDTO> menusAccesibles = df.getData();
                String mensajeOriginal = df.getMensaje();

                // Mensaje para auditoría
                String mensajeAuditoria = "Se han encontrado un total de " + (menusAccesibles != null ? menusAccesibles.size() : 0) + " menús accesibles para el rol " + rol.getNombre();

                // Reestablecer la respuesta con auditoría
                df.llenarRespuestaExitosa(mensajeOriginal, menusAccesibles, mensajeAuditoria);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<MenuDTO> creaUnMenuDelSistema(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<MenuDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener los datos del JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Obtener la empresa desde el JWT
            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            // Desencriptar el cuerpo de la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();
            MenuDTO menuDTO = new Gson().fromJson(bodyString, MenuDTO.class);

            // Si es una edición, buscar el menú existente
            Menu menu = null;
            boolean esEdicion = false;

            if (menuDTO.getId() != null) {
                menu = this.menuRepository.findByTokenIdentificadorAndRemovido(menuDTO.getId(), false);

                if (menu == null) {
                    df.setMensaje("El menú no existe o fue eliminado anteriormente");
                    return df;
                }

                esEdicion = true;
                menu.setIpEdita(ip);
                menu.setUsuarioSistemaEdita(usuarioLogin);
                menu.setFechaEdicion(new Date());
            } else {
                menu = new Menu();

                menu.setEmpresa(empresa);
                menu.setIpCrea(ip);
                menu.setUsuarioSistemaCrea(usuarioLogin);
                menu.setFechaCreacion(new Date());
            }

            menu.setRealizaAuditoria(menuDTO.getRealizaAuditoria());
            menu.setTitulo(menuDTO.getTitle());
            menu.setSubtitulo(menuDTO.getSubtitle());
            menu.setNemonico(menuDTO.getNemonico());
            menu.setTipo(menuDTO.getType());
            menu.setMostrarEnElFront(menuDTO.getMostrarEnFront());
            menu.setIcono(menuDTO.getIcon());
            menu.setLink(menuDTO.getLink());

            if ("group".equals(menuDTO.getType()) || "collapsable".equals(menuDTO.getType())) {
                menu.setEsPadre(Boolean.TRUE);
            }

            if (menuDTO.getTokenIdentificadorPadre() != null) {
                Menu menuPadre = this.menuRepository.findByTokenIdentificadorAndRemovido(menuDTO.getTokenIdentificadorPadre(), false);
                menu.setMenuPadre(menuPadre);
            } else {
                menu.setMenuPadre(null);
            }

            this.menuRepository.save(menu);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String accion = esEdicion ? "editó" : "creó";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se " + accion + " con éxito el menú con título: " + menu.getTitulo();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accion + " con éxito el menú " + menu.getTitulo() +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, menuDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<MenuDTO>> obtenerMenuDelSistema(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idMenu").descending()
            );

            Page<Menu> menuPage = this.menuRepository.findByEmpresaIdEmpresaAndRemovido(
                    empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<MenuDTO> paginacionResponse = new PaginacionResponse<>();
            List<MenuDTO> listaMenuDTO = new ArrayList<>();
            for (Menu menu : menuPage.toList()) {
                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(menu.getTokenIdentificador());
                menuDTO.setTitle(menu.getTitulo());
                menuDTO.setSubtitle(menu.getSubtitulo());
                menuDTO.setType(menu.getTipo());
                menuDTO.setNemonico(menu.getNemonico());
                menuDTO.setMostrarEnFront(menu.getMostrarEnElFront());
                menuDTO.setIcon(menu.getIcono());
                menuDTO.setLink(menu.getLink());
                menuDTO.setTokenIdentificador(menu.getTokenIdentificador());

                if (menu.getMenuPadre() != null) {
                    menuDTO.setTokenIdentificadorPadre(menu.getMenuPadre().getTokenIdentificador());
                }

                menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

                listaMenuDTO.add(menuDTO);
            }

            paginacionResponse.setData(listaMenuDTO);
            paginacionResponse.setTotalItems(menuPage.getTotalElements());

            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = menuPage.getTotalElements(); // Total de menús en el sistema
            long elementosPaginaActual = listaMenuDTO.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + totalElementos + " menús disponibles en el sistema, mostrando " + elementosPaginaActual + " en esta página";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " menús del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarMenu(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();

            MenuDTO menuDTO = new Gson().fromJson(bodyString, MenuDTO.class);

            Menu menu = this.menuRepository.findByTokenIdentificadorAndRemovido(
                    menuDTO.getId(), false
            );

            if (menu == null) {
                df.setMensaje("El menú no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Date fecha = new Date();
            menu.setRemovido(true);
            menu.setIpElimina(ip);
            menu.setUsuarioSistemaElimina(usuarioSistemaLogin);
            menu.setFechaEliminacion(fecha);

            this.menuRepository.save(menu);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String fechaFormateada = formatearFechaEspanol(fecha);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se ha eliminado con exito del sistema al menú: " + menu.getTitulo();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito el menú " + menu.getTitulo() +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<MenuDTO> verificarPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<MenuDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener los datos del JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Obtener la empresa desde el JWT
            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();
            BodyJwtValido bodyJwtValido = df2.getData();

            // Desencriptar el cuerpo de la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();
            // En este caso el request es solo el string con la clave de la pantalla
            String clavePantalla = new Gson().fromJson(bodyString, String.class);
            this.logService.info("Clave nemonico verificado: " + clavePantalla);

            List<UsuarioSistemaEmpresaRol> userRoles = this.usuarioSistemaEmpresaRolRepository.findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRemovido
                    (empresa.getTokenIdentificador(), usuarioLogin.getTokenIdentificador(), Boolean.FALSE);

            if (userRoles.isEmpty() || userRoles.size() > 1) {
                df.setLogOut(true);
                df.setMensaje("El usuario usuario asociado a la sesión no tiene asignado ningún rol válido");
                this.logService.info("El usuario usuario asociado a la sesión no tiene asignado ningún rol válido");
                //df.setNoAccess(true);

                return df;
            }

            // Se verifica con el primer elemento ya que solo deberia haber un rol por usuario y empresa
            Rol rolActual = bodyJwtValido.getRolJerarquia();

            if (rolActual == null) {
                df.setLogOut(true);
                df.setMensaje("Tu rol no está disponible en los registros");
                this.logService.info("Tu rol no está disponible en los registros");
                return df;
            }

            Menu menu = this.menuRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(empresa.getTokenIdentificador(), clavePantalla, Boolean.FALSE);

            if (menu == null) {
                df.setMensaje("La opción que estas accediendo no esta disponible");
                df.setSinAcceso(true);
                this.logService.info("El menu que estas accediendo no esta disponible");

                return df;
            }

            MenuDTO menuDTO = new MenuDTO();

            // Primero, verifica si el rol actual es administrador ya que en este caso no se necesitan permisos
            if (rolActual.getEsSuperRol() != null && rolActual.getEsSuperRol()) {
                menuDTO.setId(menu.getTokenIdentificador());
                menuDTO.setTitle(menu.getTitulo());
                menuDTO.setSubtitle(menu.getSubtitulo());
                menuDTO.setType(menu.getTipo());
                menuDTO.setNemonico(menu.getNemonico());
                menuDTO.setMostrarEnFront(menu.getMostrarEnElFront());
                menuDTO.setIcon(menu.getIcono());
                menuDTO.setLink(menu.getLink());
                menuDTO.setTokenIdentificador(menu.getTokenIdentificador());

                if (menu.getMenuPadre() != null) {
                    menuDTO.setTokenIdentificadorPadre(menu.getMenuPadre().getTokenIdentificador());
                }

                menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

                df.setCodigoEstado(200);
                df.setData(menuDTO);
                df.setMensaje("Tienes acceso porque tienes un rol de administrador");
                this.logService.info("Rol de administrador no se verifica los permisos");
                return df;
            }

            // En caso de no ser administrador revisa de entre todos los menus con el mismo nemonico(etiqueta) que exista un menu rol que coincida y le da ese permiso
            // En este caso almenos por ahora solo existe un 
            MenuEmpresaRol menuRol = this.menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovido(empresa.getIdEmpresa(), rolActual.getIdRol(), menu.getIdMenu(), Boolean.FALSE);
            if (menuRol == null) {
                df.setMensaje("La opción que estas accediendo no esta disponible");
                df.setSinAcceso(true);

                this.logService.info("El menu rol al que estas accediendo no esta disponible");
                return df;
            }

            menuDTO.setId(menu.getTokenIdentificador());
            menuDTO.setTitle(menu.getTitulo());
            menuDTO.setSubtitle(menu.getSubtitulo());
            menuDTO.setType(menu.getTipo());
            menuDTO.setNemonico(menu.getNemonico());
            menuDTO.setMostrarEnFront(menu.getMostrarEnElFront());
            menuDTO.setIcon(menu.getIcono());
            menuDTO.setLink(menu.getLink());
            menuDTO.setTokenIdentificador(menu.getTokenIdentificador());

            if (menu.getMenuPadre() != null) {
                menuDTO.setTokenIdentificadorPadre(menu.getMenuPadre().getTokenIdentificador());
            }

            menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

            df.setCodigoEstado(200);
            df.setData(menuDTO);
            df.setMensaje("Tienes acceso para realizar esta operación");
            this.logService.info("Tienes acceso para realizar esta operación");

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> obtenerRolesPorFiltro(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación del token JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el cuerpo de la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);

            // Configurar paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idRol").descending()
            );

            // Buscar en base de datos
            Page<Rol> roles = this.rolRepository.buscarPorValor(paginacionRequest.getFilter(), pageable);

            // Convertir entidades a DTOs
            PaginacionResponse<CreacionDeRol> paginacionResponse = new PaginacionResponse<>();
            List<CreacionDeRol> rolDTOList = new ArrayList<>();

            for (Rol rol : roles.toList()) {
                CreacionDeRol creacionDeRol = new CreacionDeRol();
                creacionDeRol.setTokenIdentificador(rol.getTokenIdentificador());
                creacionDeRol.setTokenIdentificadorEmpresa(rol.getEmpresa().getTokenIdentificador());
                creacionDeRol.setNombre(rol.getNombre());
                creacionDeRol.setCodigo(rol.getCodigo());
                creacionDeRol.setDescripcion(rol.getDescripcion());
                creacionDeRol.setEsSuperRol(rol.getEsSuperRol());
                creacionDeRol.setEsRolPorDefecto(rol.getEsRolPorDefecto());
                creacionDeRol.setDiasExpiracionPassword(rol.getDiasExpiracionPassword());
                creacionDeRol.setBloqueadoRelacion(rol.getBloqueado());

                rolDTOList.add(creacionDeRol);
            }

            paginacionResponse.setData(rolDTOList);
            paginacionResponse.setTotalItems(roles.getTotalElements());

            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = roles.getTotalElements(); // Total de roles que coinciden con el filtro
            long elementosPaginaActual = rolDTOList.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se encontraron " + totalElementos + " roles que coinciden con el filtro '" + paginacionRequest.getFilter() + "', mostrando " + elementosPaginaActual + " en esta página";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " roles filtrados del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<LoginResponse> cambioJerarquiaUserSistema(HttpServletRequest httpServletRequest,
                                                                                  BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<LoginResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String loginRequestString = df22.getData();

            LoginRequest loginRequest = new Gson().fromJson(loginRequestString, LoginRequest.class);

            UsuarioSistema usuario = df2.getData().getUsuarioSistema();

            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository
                    .findByUsuarioSistemaIdUsuarioSistemaAndRemovido(usuario.getIdUsuarioSistema(), false);

            List<FuncionarioJerarquiaRol> fjrList = funcionarioJerarquiaRolRepository
                    .findByFuncionario_TokenIdentificadorAndRemovidoFalseAndRolIsNotNull(
                            usuario.getFuncionario().getTokenIdentificador()
                    );


            if (fjrList.isEmpty()) {
                df.setMensaje("Tu usuario no tiene asignada ninguna jerarquía, comunícate con tu administrador");
                return df;
            }

            BodyJWTFront bodyJWTFront = new BodyJWTFront();
            LoginResponse loginResponse = new LoginResponse();

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = usuarioSistemaEmpresaRolList.get(0);

            Empresa empresa = usuarioSistemaEmpresaRol.getEmpresa();

            if (loginRequest.getTokenIdentificadorJerarquia() != null) {
                Optional<FuncionarioJerarquiaRol> seleccion = fjrList.stream()
                        .filter(fjr -> loginRequest.getTokenIdentificadorJerarquia()
                                .equals(fjr.getJerarquia().getTokenIdentificador()))
                        .findFirst();

                if (seleccion.isPresent()) {
                    FuncionarioJerarquiaRol asig = seleccion.get();
                    Jerarquia jer = asig.getJerarquia();
                    Rol rol = asig.getRol();  // puede ser null si opcional

                    // valida rol si existe
                    if (rol != null && rol.getBloqueado()) {
                        df.setMensaje("El rol de la jerarquía seleccionada está bloqueado");
                        return df;
                    }

                    RespuestaPorDefectoAuditoria<List<MenuDTO>> df4 = this.crearMenuPorRolYEmpresa(rol, empresa, false);
                    if (!df4.isExito()) {
                        df.setMensaje(df4.getMensaje());
                        return df;
                    }

                    List<MenuDTO> menuDTOList = df4.getData();

                    if (menuDTOList.isEmpty()) {
                        df.setMensaje("No tienes configurado los menu, comunicate con tu administrador");
                        return df;
                    }

                    loginResponse.setTokenIdentificadorJerarquia(jer.getTokenIdentificador());
                    loginResponse.setNombreRol(rol != null ? rol.getNombre() : "");
                    loginResponse.setTokenIdentificadorRolJerarquia(rol != null ? rol.getTokenIdentificador() : "");
                    loginResponse.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

                    bodyJWTFront.setIdentificadorRolJerarquia(rol != null ? rol.getTokenIdentificador() : "");
                    bodyJWTFront.setIdentificadorJerarquia(jer.getTokenIdentificador());
                    bodyJWTFront.setIdentificadorEmpresa(empresa.getTokenIdentificador());
                    bodyJWTFront.setIdentificadorUsuarioSistema(usuario.getTokenIdentificador());

                    RespuestaPorDefectoAuditoria<String> df3 = this.jwtProviderService.crearJwt(bodyJWTFront.toString(),
                            empresa.getIdEmpresa(),
                            this.parametroDelSistemaService);
                    if (!df3.isExito()) {
                        df.setMensaje(df3.getMensaje());
                        return df;
                    }
                    loginResponse.setJwt(df3.getData());


                    df.llenarRespuestaExitosa("Login exitoso",
                            loginResponse);
                    return df;


                } else {
                    df.setMensaje("La jerarquía seleccionada no es válida para este usuario");
                    return df;
                }
            }


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Formatea una fecha al español en el formato: "viernes, 30 de mayo del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "fecha no disponible";
        }

        try {
            // Configurar el locale para español
            Locale localeEspanol = new Locale("es", "ES");

            // Crear el formato personalizado
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", localeEspanol);

            return formatoCompleto.format(fecha);
        } catch (Exception e) {
            // En caso de error, devolver un formato simple
            SimpleDateFormat formatoSimple = new SimpleDateFormat("dd/MM/yyyy");
            return formatoSimple.format(fecha);
        }
    }

    /**
     * Método auxiliar para obtener nombres completos de un usuario
     */
    private String obtenerNombresCompletosUsuario(CreacionDeUsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener nombres completos de un UsuarioSistema
     */
    private String obtenerNombreCompletoUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de un usuario
     */
    private String obtenerIdentificacionUsuario(CreacionDeUsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        String identificacion = "N/A";

        if (usuario.getNumeroDeDocumento() != null && !usuario.getNumeroDeDocumento().trim().isEmpty()) {
            identificacion = usuario.getNumeroDeDocumento();
        } else if (usuario.getUserName() != null && !usuario.getUserName().trim().isEmpty()) {
            identificacion = usuario.getUserName();
        } else {
            String nombresCompletos = obtenerNombresCompletosUsuario(usuario);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }

    private void persistirAsignaciones(
            Funcionario funcionario,
            List<FuncionarioJerarquiaRolDTO> asignacionesDto
    ) {
        if (asignacionesDto == null) return;

        // 1) Marcar como removidas las que el front ha quitado
        List<FuncionarioJerarquiaRol> existentes = funcionarioJerarquiaRolRepository
                .findByFuncionario_TokenIdentificadorAndRemovidoFalse(
                        funcionario.getTokenIdentificador()
                );

        Set<String> jerarquiasFront = asignacionesDto.stream()
                .map(FuncionarioJerarquiaRolDTO::getTokenIdentificadorJerarquia)
                .collect(Collectors.toSet());

        for (FuncionarioJerarquiaRol asig : existentes) {
            String tokJer = asig.getJerarquia().getTokenIdentificador();
            if (!jerarquiasFront.contains(tokJer)) {
                asig.setRemovido(true);
                asig.setFechaEliminacion(new Date());
                funcionarioJerarquiaRolRepository.save(asig);
            }
        }

        String tokFunc = funcionario.getTokenIdentificador();
        for (FuncionarioJerarquiaRolDTO dto : asignacionesDto) {
            String tokJer = dto.getTokenIdentificadorJerarquia();
            String tokRol = dto.getTokenIdentificadorRol();  // puede ser null

            // 2) Buscar asignación por funcionario+jerarquía (sin filtrar removido)
            Optional<FuncionarioJerarquiaRol> opt = funcionarioJerarquiaRolRepository
                    .findByFuncionario_TokenIdentificadorAndJerarquia_TokenIdentificador(
                            tokFunc, tokJer
                    );

            // 3) Cargar la jerarquía (siempre obligatoria)
            Jerarquia jer = jerarquiaRepository
                    .findByTokenIdentificadorAndRemovido(tokJer, false);
            if (jer == null) {
                throw new IllegalStateException(
                        "Jerarquía no encontrada: " + tokJer
                );
            }

            // 4) Si viene un tokenRol, lo cargamos; si no, dejamos rol en null
            Rol rol = null;
            if (tokRol != null) {
                rol = rolRepository
                        .findByTokenIdentificadorAndRemovido(tokRol, false);
                if (rol == null) {
                    throw new IllegalStateException(
                            "Rol no encontrado: " + tokRol
                    );
                }
            }

            if (opt.isPresent()) {
                // 5a) Ya existe: solo actualizamos rol si vino en el DTO
                FuncionarioJerarquiaRol exist = opt.get();
                if (rol != null) {
                    exist.setRol(rol);
                }
                exist.setRemovido(false);
                exist.setFechaEdicion(new Date());
                funcionarioJerarquiaRolRepository.save(exist);

            } else {
                // 5b) No existe: creamos nuevo (rol puede quedar null)
                FuncionarioJerarquiaRol nuevo = new FuncionarioJerarquiaRol();
                nuevo.setFuncionario(funcionario);
                nuevo.setJerarquia(jer);
                if (rol != null) {
                    nuevo.setRol(rol);
                }
                nuevo.setRemovido(false);
                nuevo.setFechaCreacion(new Date());
                funcionarioJerarquiaRolRepository.save(nuevo);
            }
        }
    }
}
