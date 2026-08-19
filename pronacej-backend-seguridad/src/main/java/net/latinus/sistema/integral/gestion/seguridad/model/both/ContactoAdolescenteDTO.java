package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idContactoAdolescente"}, callSuper = true)
public class ContactoAdolescenteDTO extends CamposDTO{
    private Long idContactoAdolescente;
    private String tokenFichaIdentificacion;
    private Date fechaRegistro;
    private String usuarioResponsable;
    private String modalidadEntrevista;
    private String observaciones;
    private String actividades;
    private String nombreAdolescente;

    public String getNombreAdolescente() {
        return nombreAdolescente;
    }

    public void setNombreAdolescente(String nombreAdolescente) {
        this.nombreAdolescente = nombreAdolescente;
    }


}
