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
public class AuditoriaAccionesSistemaDTO extends CamposDTO implements Serializable {

    private Date fechaFinAccion;
    private Date fechaInicioAccion;

    private String tokenIdentificadorAccion;
    private String nombreAccion;

    private AuditoriaServicioRestDTO auditoriaServicioRestDTO;

    private String tokenIdentificadorMenu;
    private String nombreMenu;

    private String tokenIdentificadorRol;
    private String nombreRol;

    private String tokenIdentificadorUsuarioQueRealizaLaAccion;
    private String nombreUsuarioQueRealizaLaAccion;
    private String userNameUsuarioQueRealizaLaAccion;
    private String emailUsuarioQueRealizaLaAccion;
    private String descripcion;

    private String modulo;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
