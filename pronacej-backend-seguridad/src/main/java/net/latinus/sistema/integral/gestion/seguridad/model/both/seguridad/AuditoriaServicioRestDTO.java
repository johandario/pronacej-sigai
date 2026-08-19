package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuditoriaServicioRestDTO extends CamposDTO implements Serializable {

    private String accept;
    private String acceptLanguage;
    private Integer contentLength;
    private String contentType;
    private Date fechaRequest;
    private Date fechaResponse;
    private String headerAuthorization;
    private String headersJson;
    private String host;
    private String jsonRequest;
    private String jsonResponse;
    private String origin;
    private String platform;
    private String referer;
    private String tipoDeMetodo;
    private String url;
    private String userAgent;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
