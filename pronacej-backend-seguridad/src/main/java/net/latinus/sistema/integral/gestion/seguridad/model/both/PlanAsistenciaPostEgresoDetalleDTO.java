package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanAsistenciaPostEgresoDetalleDTO extends CamposDTO {
    private Long idPlanAsistenciaPostEgresoDetalle;
    private CatalogoDTO area;
    private String factores;
    private String objetivoGeneral;
    private String objetivoEspecifico;
    private String actividades;
    private String institucion;
    private String frecuencia;
    private String indicador;
}
