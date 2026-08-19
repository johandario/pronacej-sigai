package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class CargosJerarquiaDTO extends CamposDTO {
    private Long idCargosJerarquia;
    private Long idJerarquia;
    private String tokenIdentificadorJerarquia;
    private Boolean esJefe = false;
    private String nombre;
    private String departamento;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
    
}
