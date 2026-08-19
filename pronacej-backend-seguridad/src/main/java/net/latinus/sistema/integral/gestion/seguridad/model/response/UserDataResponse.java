package net.latinus.sistema.integral.gestion.seguridad.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
public class UserDataResponse implements Serializable {

    private String id;
    private String name;
    private String email;
    private String avatar;
    private String status = "online";

    private String rol;
    private String empresa;
    private String telefono;

    private String username;

    private String tokenReseteoContrasenia;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
