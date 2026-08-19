package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPlanTratamientoIndSeguiDetalle"}, callSuper = true)
public class PlanTratamientoIndSeguiDetalleDTO extends CamposDTO implements Serializable {
    private Long idPlanTratamientoIndSeguiDetalle;
    private PlanTratamientoIndIntervDTO planTratamientoIndInterv;
    private CatalogoDTO frecuencia;
    private CatalogoDTO frecuenciaParticipacion;
    private CatalogoDTO situacionActual;
    private CatalogoDTO actitud;
    private CatalogoDTO aprovechamiento;
    private Date fechaInicio;
    private Date fechaFin;
    private String observaciones;
    private Boolean indicadorDeficiente;
    private Boolean indicadorEnProceso;
    private Boolean indicadorLogrado;
    private String analisis;
}
