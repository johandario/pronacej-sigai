package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Data
@EqualsAndHashCode(callSuper = true)
public class InformeTecnicoSustentatorioDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String motivo;
    private String criteriosSeleccion;
    private String analisisPsicologico;
    private String analisisSocial;
    private String analisisConductual;
    private String analisisFamiliar;
    private String propuestaActividadFormativa;
    private String importanciaParticipacionAdolescente;
    private String objetivosConseguir;
    private Float duracion;
    private String conclusiones;
    private String recomendaciones;
    
    // Campos adicionales para la tabla y auditoría
    private String nombreCompletoUsuarioCreacion;
    
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