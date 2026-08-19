package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class CamposDTO {

    private Boolean esEdicion = false;
    private String tokenIdentificador;
    private String tokenIdentificadorEmpresa;

    private Date fechaCreacion;
    private String ipCrea;

    private String nombreUsuarioCrea;
    private String nombreUsuarioEdita;
    private String nombreUsuarioElimina;

    private Map<String, Boolean> controlesMap;

    @Override
    public String toString() {
        try {
            Gson gson = new GsonBuilder().setDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER).create();
            return gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger logger = LoggerFactory.getLogger(e.getClass());
            logger.error(e.toString());
            return "";
        }
    }
}
