package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanAsistenciaPostEgresoDTO extends CamposDTO implements Serializable {
    private Long idPlanAsistenciaPostEgreso;
    private String nombreEstado;
    private CatalogoDTO estado;
    private Date fechaInicio;
    private Date fechaFin;
    private String fecCreacion;
    private String fecInicio;
    private String fecFin;
    private List<PlanAsistenciaPostEgresoDetalleDTO> planDetalle;
    private String tokenFichaIdenticacion;
    private Long idFichaIdentificacion;
}
