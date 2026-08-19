package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonaRelacionadaDTO extends CamposDTO implements Serializable {

    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private String tipoIdentificacion;
    private String numeroDocumento;
    private String modalidadEstudio;
    private String nivelEBR;
    private String nivelSuperior;  
    private String nivelEBA;
    private String ocupacion;
    private String estadoCivil;
    private String observaciones;
    private String tipoParentesco;
    private Date fechaNacimiento;

    private Long idPersonaRelacionada;
    private String primerNombre;
    private String segundoNombre;
    private String tipoSexo;
    private  String telefono;
    private String parentesco;
    
    private String tokenIdentificadorEvaluacionSocial;
    private String tokenIdentificadorCondicionLaboral;
    private String otros;
    private BigDecimal ingresoPromedio;
    private Long numeroHijos;
    private Boolean esResponsableEconom;

    private ArrayList<String> informacionUbicacionesEliminar;
    private ArrayList<InformacionUbicacionDTO> informacionUbicaciones;

    private String esTutor;
    private String visitaAutorizada;
    private String fallecido;

    private String tokenIdentificadorFicha;
    private String relacionAfectiva;
    private String rolesInfluencias;

    private String tipoOcupacion;
    private Boolean enfermo;
    

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
