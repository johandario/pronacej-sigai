package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioJerarquiaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermisoRolUsuarioDTO extends CamposDTO {
    private String fechaCreacionTexto;
    private FuncionarioDTO funcionario;
    private List<RolDTO> roles;
    private CatalogoDTO tipoPermiso;
    private CatalogoDTO tipoAsignacion;
    private List<PermisoRolUsuarioMenuDTO> menus;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
