package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

/**
 * Clase DTO para el manejo de Informes de Seguimiento PII
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InformeSeguimientoPIIDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String tokenIdentificadorInformeTecnico;
    private String motivoIngreso;
    private String antecedentesOrganicidad;
    private String tecnicasUtilizadas;
    private String observacionConductual;
    private String evaluacionPlanPsicologica;
    private String evaluacionPlanSocial;
    private String evaluacionPlanConductual;
    private String evaluacionPlanFamiliar;
    private String evaluacionPlanEducativa;
    private String evaluacionPlanLaboral;
    private String tokenIdentificadorNivelRiesgo;
    private String conclusiones;
    private String recomendaciones;
    
    private List<InstrumentoEvaluacionDTO> listaInstrumentosAplicados;
    
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