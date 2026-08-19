package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermisoRolUsuarioMenuAccionDTO extends CamposDTO {
    private String tokenCatalogoAccion;
    private String nemonicoCatalogoAccion;
    private Boolean activo;
}
