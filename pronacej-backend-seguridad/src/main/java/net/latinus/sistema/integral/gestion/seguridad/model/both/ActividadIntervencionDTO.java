package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActividadIntervencionDTO extends CamposDTO implements Serializable {
    private Long idActividadIntervencion;
    private Long idPlanTratIndInterv;
    private String subactividad;
    private CatalogoDTO frecuencia;
    private Date fechaInicio;
    private Date fechaFin;

}
