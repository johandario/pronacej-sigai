package net.latinus.sistema.integral.gestion.seguridad.service;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.http.ResponseEntity;

public interface ServiciosExternos {

    /**
     * Consulta DataSunat, se valida una sesión
     *
     * @param httpServletRequest List<String> Lista de receptores de correo.
     * @param ruc  String
     * @return RespuestaPorDefectoAuditoria<ResponseEntity<String>>
     */
    RespuestaPorDefectoAuditoria<ResponseEntity<String>> dataSunat(HttpServletRequest httpServletRequest,
                                                                   String ruc);


    /**
     * Consulta Data se valida una sesión
     *
     * @param httpServletRequest List<String> Lista de receptores de correo.
     * @param dni  String
     * @return RespuestaPorDefectoAuditoria<ResponseEntity<String>>
     */
    RespuestaPorDefectoAuditoria<ResponseEntity<String>> data(HttpServletRequest httpServletRequest,
                                                                   String dni);
}
