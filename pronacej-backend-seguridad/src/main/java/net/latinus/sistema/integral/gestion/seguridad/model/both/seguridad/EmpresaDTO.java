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
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmpresaDTO extends CamposDTO implements Serializable {

    private String nombre;
    private String descripcion;

    private String nombreCorto;

    private String urlPagina;

    private String urlLogo;

    private String colorPrimarioHex;
    private String colorSecundarioHex;

    private String userNameAlfresco;
    private String constraseniaAlfresco;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
