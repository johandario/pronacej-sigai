package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class SituacionEducativaLaboralDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private List<SituacionEducativaLaboralOcioDTO> listaSituEducLaboOcio;
    private List<LaboralDTO> listaLaboral;
    private AreasSituacionEducativaLaboralOcioDTO areas;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

    
}
