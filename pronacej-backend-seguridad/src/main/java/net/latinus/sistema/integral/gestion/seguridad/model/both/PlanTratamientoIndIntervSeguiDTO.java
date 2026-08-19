package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.request.Serializable;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanTratamientoIndIntervSeguiDTO extends CamposDTO implements Serializable {
    private Long idPlanTratamientoIndIntervSegui;
    private PlanTratamientoIndIntervDTO actividad;
    private Date fecha;
    private String horaInicio;
    private String horaFin;
    private String observaciones;
}
