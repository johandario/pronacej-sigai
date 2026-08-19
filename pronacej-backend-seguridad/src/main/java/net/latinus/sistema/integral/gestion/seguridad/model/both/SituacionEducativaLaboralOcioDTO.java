package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class SituacionEducativaLaboralOcioDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String centroEstudios;
    private String tokenIdentificadorSituacionEducativa;
    private String tokenIdentificadorModalidadEducativa;
    private String tokenIdentificadorModalidadEstudio;
    private String tokenIdentificadorRendimientoEducativo;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
    
}
