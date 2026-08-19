package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.AuditoriaServicioRest;
import org.springframework.http.HttpMethod;

import java.util.Date;


public interface AuditoriaServicioRestService {

    /**
     * Guarda un consumo a un servicio rest
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param jsonRequest String json request.
     * @param response String
     * @param fechaInicio Date fecha que inicio la acción
     * @param httpMethod HttpMethod
     * @param url String
     *
     * @return AuditoriaServicioRest
     */
    AuditoriaServicioRest guardarServicioRest(HttpServletRequest httpServletRequest,
                                              String jsonRequest,
                                              String response,
                                              Date fechaInicio,
                                              HttpMethod httpMethod, String url);
}
