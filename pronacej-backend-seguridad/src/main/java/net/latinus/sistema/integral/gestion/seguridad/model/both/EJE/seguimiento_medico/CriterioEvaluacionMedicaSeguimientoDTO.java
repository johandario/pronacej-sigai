package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorCriterioHijo"}, callSuper = true)
public class CriterioEvaluacionMedicaSeguimientoDTO extends CamposDTO implements Serializable {

    private String tokenIdentifidorCriterioPadre;
    private String tokenIdentificadorCriterioHijo;
    private String detalle;
    private String nombreCriterioPadre;
    private String nombreCriterioHijo;

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
