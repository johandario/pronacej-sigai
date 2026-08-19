package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActividadIntervencionSeguimientoDTO extends CamposDTO implements Serializable {

    private Long idActividadIntervencionSeguimiento;
    private Long idActividadIntervencion;
    private Date fecha;
    private String horaInicio;
    private String horaFin;
    private String observaciones;
}
