package net.latinus.sistema.integral.gestion.seguridad.model.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Data
public class PaginacionRequest implements Serializable {

    private Integer page;
    private Integer size;
    private String sort;
    private String direction;
    private String filter;
    private String tokenIdentificador;

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
