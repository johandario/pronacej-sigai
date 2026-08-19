package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

/**
 * Clase DTO para el manejo de Instrumentos de Evaluación
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstrumentoEvaluacionDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorInformeSeguimiento;
    private String tokenIdentificadorTipoInstrumento;
    
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