package net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorFichaMedica"},callSuper = true)
public class FichaMedicaDocumentoDTO extends CamposDTO {
    private String tokenIdentificadorFichaMedica;
    private DocumentoDTO documentoDTO;

    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
