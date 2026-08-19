package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorFichaIngreso"},callSuper = true)
public class FichaIngresoDocumentoDTO extends CamposDTO {

    private String tokenIdentificadorFichaIngreso;
    private DocumentoDTO documentoDTO;
    private String tokenFichaIdentificacion;

    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
