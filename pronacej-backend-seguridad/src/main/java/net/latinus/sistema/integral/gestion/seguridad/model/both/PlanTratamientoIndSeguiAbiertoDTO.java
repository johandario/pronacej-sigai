package net.latinus.sistema.integral.gestion.seguridad.model.both;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlanTratamientoIndSeguiAbiertoDTO extends CamposDTO {
    private Long idPlanTratamientoIndSeguiAbierto;
    private Date fecha;
    private String hora;
    private String descripcion;
    private String tokenPtiInterv;
}
