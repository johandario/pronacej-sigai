package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class LaboralDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String experienciaLaboral;
    private String tokenIdentificadorOcupacionLaboral;
    private String tokenIdentificadorModalidadLaboral;
    private String tokenIdentificadorRecursosApoyoLaboral;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
