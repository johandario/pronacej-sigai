package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class EvaluacionSocialArtefactoDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorEvaluacionSocial;
    private String tokenIdentificadorArtefactosVivienda;
    private Integer cantidad;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
