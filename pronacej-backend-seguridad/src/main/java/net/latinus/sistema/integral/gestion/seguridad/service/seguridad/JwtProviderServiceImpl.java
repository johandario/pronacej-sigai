package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJWTFront;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtProviderServiceImpl implements JwtProviderService {

    @Value("${application.jwt.secret}")
    private String tokenConsumoServiciosDirecto;

    private final LogService logService = new LogService(JwtProviderServiceImpl.class);

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioSistemaRepository usuarioSistemaRepository;

    @Autowired
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Autowired
    private UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository;

    @Autowired
    private JerarquiaRepository jerarquiaRepository;

    /**
     * Verifica si el header del request coincide con el registrado en el backend
     *
     * @param httpServletRequest Objeto HttpServletRequest de tomcat.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> verificarConsumoDirecto(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            String headerAuth = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (headerAuth == null || headerAuth.isEmpty()) {
                df.setMensaje("No se recibio un header de autorización");
                return df;
            }

            String bearer = "Bearer ";
            if (headerAuth.contains(bearer)) {
                headerAuth = headerAuth.replace(bearer, "").trim();
            }

            if (!headerAuth.equals(this.tokenConsumoServiciosDirecto)) {
                df.setMensaje("No tienes los permisos para realizar esta operación");
                return df;
            }

            df.llenarRespuestaExitosa("La autorización coincide con la registrada en los properties", true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Devuelve un jwt con el subject enviado
     *
     * @param subject                    String que se va a insertar en el jwt.
     * @param idEmpresa                  Long id de la empresa.
     * @param parametroDelSistemaService ParametroDelSistemaService servicio parametro del sistema.
     * @return RespuestaPorDefectoAuditoria<String>
     */
    @Override
    public RespuestaPorDefectoAuditoria<String> crearJwt(String subject, Long idEmpresa,
                                                         ParametroDelSistemaService parametroDelSistemaService) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {

            ParametroDelSistema parametroDelSistemaJWT = parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_JWT_SECRET, idEmpresa
            );

            if (parametroDelSistemaJWT == null) {
                df.setMensaje("No se pudo encontrar el secreto para el jwt, consulta a tu administrador");
                return df;
            }

            ParametroDelSistema parametroDelSistemaSegundos = parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_JWT_SEGUNDOS_VENCIMIENTO, idEmpresa
            );

            if (parametroDelSistemaSegundos == null) {
                df.setMensaje("No se pudo encontrar los segundos de vencimientos para el jwt, consulta a tu administrador");
                return df;
            }

            long segundos = Long.parseLong(parametroDelSistemaSegundos.getValor());

            logService.info("Subject del jwt: " + subject);

            Date fechaIssued = new Date();
            String jwt = Jwts.builder()
                    .subject(subject)
                    .issuedAt(fechaIssued)
                    .expiration(new Date(fechaIssued.getTime() + (segundos * 1000)))
                    .signWith(this.getSecretKey(parametroDelSistemaJWT.getValor()))
                    .compact();


            df.llenarRespuestaExitosa("La autorización coincide con la registrada en los properties", jwt);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private SecretKey getSecretKey(String jwtSecret) {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Devuelve un boolean para detectar si el jwt aún no esta expirado
     *
     * @param jwt          String jwt.
     * @param tokenEmpresa Long id de la empresa.
     * @return boolean
     */
    @Override
    public boolean validDateToken(String jwt, String tokenEmpresa) {
        try {

            ParametroDelSistema parametroDelSistemaJWT = parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_JWT_SECRET, false
            );

            if (parametroDelSistemaJWT == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWt no encontrado");
            }

            String valorKey = parametroDelSistemaJWT.getValor();

            if (valorKey == null || valorKey.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valor de la llave incorrecta");

            }

            final var expirationDate = this.getExpirationDate(jwt, getSecretKey(valorKey));
            return expirationDate.after(new Date());
        } catch (Exception ex) {
            LogService logService = new LogService(ex.getClass());
            logService.error("Ha ocurrido un error: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida");
        }
    }

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<BodyJwtValido> con las entidades de la db rol, usuario sistema y empresa
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @return RespuestaPorDefectoAuditoria<BodyJwtValido>
     */
    @Override
    public RespuestaPorDefectoAuditoria<BodyJwtValido> obtenerBodyJwtApp(HttpServletRequest httpServletRequest) {
        return this.obtenerBodyJwt(httpServletRequest, true);
    }

    @Override
    public RespuestaPorDefectoAuditoria<BodyJwtValido> obtenerBodyJwtAppNoValidarSesion(HttpServletRequest httpServletRequest) {

        return this.obtenerBodyJwt(httpServletRequest, false);
    }

    private RespuestaPorDefectoAuditoria<BodyJwtValido> obtenerBodyJwt(HttpServletRequest httpServletRequest, Boolean validarFecha) {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df = new RespuestaPorDefectoAuditoria<>();

        try {

            String header = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (header == null || header.isBlank()) {
                df.setMensaje("El request no presenta un header");
                df.setLogOut(true);
                return df;
            }

            String bearer = "Bearer ";
            if (header.contains(bearer)) {
                header = header.replace("Bearer ", "");
            }

            String tokenEmpresa = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_TOKEN_EMPRESA);

            String nemonicoMenu = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_NEMONICO_MENU);

            if (tokenEmpresa == null || tokenEmpresa.isEmpty()) {
                df.setMensaje("No se recibio el identificador de la empresa");
                df.setLogOut(true);
                return df;
            }

            if (validarFecha) {
                if (!this.validDateToken(header, tokenEmpresa)) {
                    df.setMensaje("Tu sesión ha expirado");
                    df.setLogOut(true);
                    return df;
                }
            }

            ParametroDelSistema parametroDelSistemaJWT = parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_JWT_SECRET, false
            );

            if (parametroDelSistemaJWT == null) {
                df.setMensaje("No se pudo encontrar el secreto para el jwt, consulta a tu administrador");
                df.setLogOut(true);
                return df;
            }

            String valorKey = parametroDelSistemaJWT.getValor();

            if (valorKey == null || valorKey.isEmpty()) {
                df.setMensaje("El valor de la key del jwt es nula, comunicate con tu administrador");
                return df;
            }

            String subject = this.getClaimsFromToken(
                    header, Claims::getSubject, this.getSecretKey(valorKey)
            );

            BodyJWTFront bodyJWTFront = new Gson().fromJson(subject, BodyJWTFront.class);

            Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(
                    bodyJWTFront.getIdentificadorRolJerarquia(), false
            );

            if (rol == null) {
                df.setMensaje("El rol del usuario no se econtro o fue eliminado anteriormente");
                return df;
            }

            if (rol.getBloqueado()) {
                df.setMensaje("Tu rol está bloqueado para ser usado consulta a tu administrador");
                return df;
            }

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(
                    bodyJWTFront.getIdentificadorEmpresa(), false
            );

            if (empresa == null) {
                df.setMensaje("La empresa del usuario no existe");
                return df;
            }

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            if (empresa.getBloqueado()) {
                df.setMensaje("Tu empresa está bloqueada para ser usada, consulta a tu administrador");
                return df;
            }

            UsuarioSistema usuarioSistema = this.usuarioSistemaRepository.findByTokenIdentificadorAndRemovido(
                    bodyJWTFront.getIdentificadorUsuarioSistema(), false
            );

            if (usuarioSistema == null) {
                df.setMensaje("El usuario no existe");
                return df;
            }

            if (usuarioSistema.getBloqueado()) {
                df.setMensaje("Tu usuario está bloqueado para ser usado, consulta a tu administrador");
                return df;
            }

//            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = this.usuarioSistemaEmpresaRolRepository.
//                    findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRolTokenIdentificadorAndRemovido(
//                            empresa.getTokenIdentificador(), usuarioSistema.getTokenIdentificador(),
//                            rol.getTokenIdentificador(), false
//                    );

            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository.findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRemovido(
                    empresa.getTokenIdentificador(), usuarioSistema.getTokenIdentificador(), false);

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = usuarioSistemaEmpresaRolList.get(0);


//            if (usuarioSistemaEmpresaRol.getBloqueado()) {
//                df.setMensaje("Has sido bloqueado, contactate con tu administrador");
//                return df;
//            }

            Jerarquia jerarquia = this.jerarquiaRepository.findByTokenIdentificadorAndRemovido(
                bodyJWTFront.getIdentificadorJerarquia(), false
            );

            if (jerarquia == null) {
                df.setMensaje("La jerarquia no existe");
                return df;
            }

            Rol roljerarquia = this.rolRepository.findByTokenIdentificadorAndRemovido(
                    bodyJWTFront.getIdentificadorRolJerarquia(), false
            );

            if (roljerarquia == null) {
                df.setMensaje("El rol del usuario no se econtro o fue eliminado anteriormente");
                return df;
            }

            if (roljerarquia.getBloqueado()) {
                df.setMensaje("Tu rol está bloqueado para ser usado consulta a tu administrador");
                return df;
            }

            BodyJwtValido bodyJwtValido = new BodyJwtValido();
            bodyJwtValido.setEmpresa(empresa);
            bodyJwtValido.setRol(rol);
            bodyJwtValido.setUsuarioSistema(usuarioSistema);
            bodyJwtValido.setJwt(header);
            bodyJwtValido.setUsuarioSistemaEmpresaRol(usuarioSistemaEmpresaRol);
            bodyJwtValido.setRolJerarquia(roljerarquia);
            bodyJwtValido.setJerarquia(jerarquia);
            if (nemonicoMenu != null) {
                bodyJwtValido.setNemonicoMenu(nemonicoMenu);
            }

            df.llenarRespuestaExitosa("La sesión pudo ser válidada correctamente",
                    bodyJwtValido);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
            df.setLogOut(true);
        }
        return df;
    }

    private Date getExpirationDate(String token, SecretKey secretKey) {
        return this.getClaimsFromToken(token, Claims::getExpiration, secretKey);
    }

    private <T> T getClaimsFromToken(String string, Function<Claims, T> resolver, SecretKey secretKey) {
        return resolver.apply(this.signToken(string, secretKey));
    }

    private Claims signToken(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
