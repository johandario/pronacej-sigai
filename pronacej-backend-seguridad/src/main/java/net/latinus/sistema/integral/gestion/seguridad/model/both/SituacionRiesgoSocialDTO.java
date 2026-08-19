package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SituacionRiesgoSocialDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String anteDeliFami;
    private String primManiInfrAdol;
    private Boolean evasionHogar;
    private String estadoSaludGeneral;
    private String problemasLegales;
    private String observaciones;
    
    private String nombreCompletoUsuarioCreacion;
}
