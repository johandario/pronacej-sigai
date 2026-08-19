package net.latinus.sistema.integral.gestion.seguridad.model.both.institucion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idAdolescenteDerivado"}, callSuper = true)
public class AdolescenteDerivadoInstDTO extends CamposDTO{
    private Long idAdolescenteDerivado;
    private String tokenFichaIdentificacion;
    private Date fechaRegistro;
    private Date fechaDerivacion;
    private String departamento;
    private String tiempoServicio;
    private String servicio;
    private String personaResponsable;
    private String estado;
    private RegistroInstitucionDTO institucion;
    private String nombreAdolescente;
    private String nombreInstitucion;



    public String getNombreAdolescente() {
        return nombreAdolescente;
    }

    public void setNombreAdolescente(String nombreAdolescente) {
        this.nombreAdolescente = nombreAdolescente;
    }

    public String getnombreInstitucion() {
        return nombreInstitucion;
    }

    public void setNombreInstitucion(String nombreInstitucion) {
        this.nombreInstitucion = nombreInstitucion;
    }



}
