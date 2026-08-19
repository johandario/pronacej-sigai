package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPlanTratIndInterv"}, callSuper = true)
public class PlanTratamientoIndIntervDTO extends CamposDTO implements Serializable {
    private Long idPlanTratIndInterv;
    private String version;
    private Boolean reajuste;
    private Boolean activo;
    private String fundamentacionReajuste;
    private Date fechaReajuste;
    private CatalogoDTO dimension;
    private String objetivo;
    private String actividadPrograma;
    private String equipoResponsable;
    private String tiempoEstimado;
    private String numAtencionIndividual;
    private String numAtencionGrupal;
    private String lugar;
    private CatalogoDTO modalidad;
    private CatalogoDTO frecuencia;
    private String descripcion;
    private Date fechaInicio;
    private Date fechaFin;
    private String tokenFichaIdentificacion;

}
