package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorSeguimiento"},callSuper = true)
public class PlanTratamientoIndSeguiDocumentoDTO extends CamposDTO {
    private String tokenIdentificadorSeguimiento;
    private DocumentoDTO documentoDTO;

    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
