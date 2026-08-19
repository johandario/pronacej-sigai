package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ClasificacionEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonaRelacionadaEnfermedadDTO extends CamposDTO implements Serializable {

    private String tokenTipoEnfermedad;
    private String detalle;
    private Boolean enfermedadActiva;
    private String tokenIdentificadorPersona;

    private String nombreEnfermedad;
    private String parentescoPersona;
    private String nombrePersona;

    private ClasificacionEnfermedadDTO clasificacionEnfermedad;
    private CatalogoDTO tipoParentesco;
    private CatalogoDTO sexoParentesco;

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(
                    EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return "";
        }
    }

}
