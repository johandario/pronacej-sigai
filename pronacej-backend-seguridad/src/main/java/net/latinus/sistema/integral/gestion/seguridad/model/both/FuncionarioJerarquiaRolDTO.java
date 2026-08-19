package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.Serializable;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class FuncionarioJerarquiaRolDTO extends CamposDTO
        implements Serializable {

    private Long id;
    private Long idJerarquia;
    private String jerarquia;
    private String tokenIdentificadorJerarquia;
    private Long idRol;
    private String rol;
    private String tokenIdentificadorRol;
    private String tokenIdentificadorCargo;


    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
