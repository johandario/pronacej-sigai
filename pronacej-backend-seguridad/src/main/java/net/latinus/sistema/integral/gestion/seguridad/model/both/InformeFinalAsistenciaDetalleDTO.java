package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class InformeFinalAsistenciaDetalleDTO extends CamposDTO implements Serializable {
    private Long idInformeFinalAsistenciaDetalle;
    private CatalogoDTO area;
    private String objetivoGeneral;
    private String objetivoEspecifico;
    private String actividades;
    private String descripcionActividad;
    private String logro;
    private String dificultad;
}
