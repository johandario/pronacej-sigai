package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorDatosFamiliares"}, callSuper = true)
public class DatosFamiliaresDocumentoDTO extends CamposDTO {
    private String tokenIdentificadorDatosFamiliares;
    private DocumentoDTO documentoDTO;
    private String tokenFichaIdentificacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}