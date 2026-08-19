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
@EqualsAndHashCode(of = {"tokenIdentificadorFichaIdentificacion"},callSuper = true)
public class EvaluacionDomiciliariaDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String tokenIdentificadorCentro;
    private JerarquiaDTO centro;
    private Date fechaRegistro;
    private Date fechaEntrevista;
    private String tokenIdentificadorPersonaRelacionada;
    private String otraPersonaRelacionada;
    private Float duracionVista;
    private Boolean visitaRealizada;
    private String motivoNoVisita;
    private String objetivoGeneral;
    private String desarrolloVisitaDomiciliaria;
    private String caracteristicasDomicilioVisitado;
    private String conclusiones;
    private String recomendaciones;
    // Campos medio cerrado
    private String dinamicaFamiliarDisfuncional;
    private String caracteristicasEntornoSocialMC;
    private String factoresProtectores;
    // Campos medio abierto
    private String factoresRiesgoFamilia;
    private String factoresRiesgoSocial;
    private String factoresProtectoresFamilia;
    private String factoresProtectoresSocial;
    
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
