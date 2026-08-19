package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatosFamiliaresDTO extends CamposDTO implements Serializable {

    private String tipoFamilia;
    private String organizacionFamiliar;
    private String ejercicioAutoridad;
    private String entornoFamiliar;
    //Booleanos S N
    private String relacionIntraFamiliarPadres;
    private String relacionIntraFamiliarFilial;
    private String relacionIntraFamiliarParentales;
    private String relacionIntraFamiliarPareja;
    private String partidaNacimiento;
    private String bautismo;
    private String primeraComunion;
    private String confirmacion;

    private String observacionesRelacionIntrafamiliar;
    private String causaAusenciaPadres;
    private String religion;

    private String  otroSacramento;
    private String tokenIdentificadorFicha;

    private String tipoSacramento;

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
