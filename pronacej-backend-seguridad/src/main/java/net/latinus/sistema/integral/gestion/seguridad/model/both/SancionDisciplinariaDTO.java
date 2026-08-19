package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idSancionDisciplinaria"}, callSuper = true)
public class SancionDisciplinariaDTO extends CamposDTO implements Serializable{

    private Long idSancionDisciplinaria;
    private FichaIdentificacionDTO fichaIdentificacion;
    private String motivo;
    private Date fechaInicio;
    private Date fechaFin;
    private Date fechaRegistro;
    private String nroResolucion;
    private CatalogoDTO tipificacionFalta;
    private JerarquiaDTO centro;
    private JerarquiaDTO programa;
    private JerarquiaDTO ambiente;
    private String falta;
    private String sancion;
    private String observacion;
    private String nombreMotivo;
    private String nombreAdolescente;
    private String nombreTipificacion;
    private List<DocumentoDTO> documentoDTOList;
    private String tokenIdentificadorSancion;

    public String getNombreAdolescente() {
        return nombreAdolescente;
    }

    public void setNombreAdolescente(String nombreAdolescente) {
        this.nombreAdolescente = nombreAdolescente;
    }

    public String getNombreMotivo() {
        return nombreMotivo;
    }

    public void setNombreMotivo(String nombreMotivo) {
        this.nombreMotivo = nombreMotivo;
    }

    public String getNombreTipificacion() {
        return nombreTipificacion;
    }

    public void setNombreTipificacion(String nombreTipificacion) {
        this.nombreTipificacion = nombreTipificacion;
    }

    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}
