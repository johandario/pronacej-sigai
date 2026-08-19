package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Data
@EqualsAndHashCode(callSuper = true)
public class AreasSituacionEducativaLaboralOcioDTO extends CamposDTO implements Serializable {

   private String tokenIdentificadorFichaIdentificacion;
   
   // Área Educativa
   private String actitudEstudios;
   private String desarrolloEducativo;
   private String interesesVocacionales; 
   private String observacionesEducativas;

   // Área Laboral
   private String actitudEmpleo;
   private String capacitacionesEmpleabilidad;
   private String observacionesLaborales;

   // Área Ocio
   private String pasatiempos;
   private String talentos;
   private String participacionGrupal;
   private String usoTiempo;
   private String observacionesOcio;
   
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
           LogService logService = new LogService(ex.getClass());
           logService.error("Ha ocurrido un error: {}", ex.getMessage(), ex);
           return "";
       }
   }
}
