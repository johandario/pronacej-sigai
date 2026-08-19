package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.AuditoriaServicioRest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.AuditoriaServicioRestRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AuditoriaServicioRestServiceImpl implements AuditoriaServicioRestService {

    private AuditoriaServicioRestRepository auditoriaServicioRestRepository;
    private JwtProviderService jwtProviderService;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public AuditoriaServicioRest guardarServicioRest(HttpServletRequest httpServletRequest, String jsonRequest, String response, Date fechaInicio,
                                                     HttpMethod httpMethod,
                                                     String url) {
        Date fechafin = new Date();

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtAppNoValidarSesion(httpServletRequest);

        if (!df2.isExito()) {
            this.logService.warn("Consumo de un servicio rest con un jwt inválido");
        }

        AuditoriaServicioRest auditoriaServicioRest = new AuditoriaServicioRest();
        auditoriaServicioRest.setJsonRequest(jsonRequest);
        auditoriaServicioRest.setFechaRequest(fechaInicio);
        auditoriaServicioRest.setFechaResponse(fechafin);
        auditoriaServicioRest.setJsonResponse(response);
        auditoriaServicioRest.setTipoDeMetodo(httpMethod.name());
        auditoriaServicioRest.setUrl(url);


        String ip = httpServletRequest != null ? httpServletRequest.getRemoteAddr() : null;
        auditoriaServicioRest.setIpCrea(ip);

        //Datos del servicio
        BodyJwtValido bodyJwtValido = df2.getData();
        if (bodyJwtValido != null) {
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            Empresa empresa = bodyJwtValido.getEmpresa();

            auditoriaServicioRest.setUsuarioSistemaCrea(usuarioSistema);

            auditoriaServicioRest.setEmpresa(empresa);
        }

        if (httpServletRequest != null) {
            auditoriaServicioRest.setAccept(Objects.requireNonNull(httpServletRequest).getHeader(HttpHeaders.ACCEPT));
            auditoriaServicioRest.setHeaderAuthorization(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
            auditoriaServicioRest.setAcceptLanguage(httpServletRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
            String contentLength = httpServletRequest.getHeader(HttpHeaders.CONTENT_LENGTH);
            if (contentLength != null) {
                auditoriaServicioRest.setContentLength(Integer.valueOf(contentLength));
            }
            auditoriaServicioRest.setContentType(httpServletRequest.getHeader(HttpHeaders.CONTENT_TYPE));
            auditoriaServicioRest.setHost(httpServletRequest.getHeader(HttpHeaders.HOST));
            auditoriaServicioRest.setOrigin(httpServletRequest.getHeader(HttpHeaders.ORIGIN));

            auditoriaServicioRest.setPlatform(httpServletRequest.getHeader("sec-ch-ua-platform"));
            auditoriaServicioRest.setReferer(httpServletRequest.getHeader(HttpHeaders.REFERER));
            auditoriaServicioRest.setUserAgent(httpServletRequest.getHeader(HttpHeaders.USER_AGENT));

            Enumeration names = httpServletRequest.getHeaderNames();

            if (names != null) {
                JSONObject jsonObject = new JSONObject();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    Enumeration values = httpServletRequest.getHeaders(name);
                    if (values != null) {
                        while (values.hasMoreElements()) {
                            String value = (String) values.nextElement();
                            jsonObject.put(name, value);
                        }
                    }
                }
                auditoriaServicioRest.setHeadersJson(jsonObject.toString());
            }
        }else{
            this.logService.warn("Consumo servicio rest con datos del request vacio");
        }


        return this.auditoriaServicioRestRepository.save(auditoriaServicioRest);
    }
}
