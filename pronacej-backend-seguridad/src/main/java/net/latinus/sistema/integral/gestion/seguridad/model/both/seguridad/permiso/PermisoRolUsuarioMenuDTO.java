package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermisoRolUsuarioMenuDTO extends CamposDTO {
    private String tokenMenu;
    private String nemonicoMenu;
    private List<PermisoRolUsuarioMenuAccionDTO> acciones;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
