package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.List;

@Data
public class SeguimientoEducativoDTO {

    private String tokenIdentificadorSeguimiento;
    private List<DocumentoDTO> documentoDTOList;

    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
