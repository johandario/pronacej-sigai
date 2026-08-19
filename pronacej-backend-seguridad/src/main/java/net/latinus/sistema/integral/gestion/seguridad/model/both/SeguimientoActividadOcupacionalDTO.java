package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeguimientoActividadOcupacionalDTO extends CamposDTO{

    private String tokenIdentificadorActividadOcupacional;
    private String actividad;
    private Boolean vigente;
    private String observaciones;
    private Date fechaActividad;

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            LogService logService = new LogService(ex.getClass());
            logService.error("Error en toString(): {}", ex.getMessage(), ex);
            return "";
        }
    }

}
