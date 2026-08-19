package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class InformeFinalAbiertoMedidasDTO extends CamposDTO implements Serializable {
    private Long idInformeFinalAbiertoMedidas;
    private String medidaAccesoria;
    private String accion;
    private String objetivo;
    private String analisisCualitativo;
}
