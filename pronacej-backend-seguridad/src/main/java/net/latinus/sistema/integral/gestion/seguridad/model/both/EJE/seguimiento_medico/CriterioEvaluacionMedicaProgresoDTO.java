package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorCriterioHijo"}, callSuper = true)
public class CriterioEvaluacionMedicaProgresoDTO extends CamposDTO implements Serializable {

    private String tokenIdentifidorCriterioPadre;
    private String tokenIdentificadorCriterioHijo;

    private String nombreCriterioPadre;
    private String nombreCriterioHijo;

    private String tokenIdentificadorLado;
    private String nombreLado;

    private String tokenIdentificadorUbicacion;
    private String nombreUbicacion;

    private String detalle;
    private Boolean presente;

    private CatalogoDTO criterioPadre;
    private CatalogoDTO criterioHijo;
    private CatalogoDTO ladoSigno;
    private CatalogoDTO ubiacionSigno;

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
