package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermisoRolUsuarioNombresDTO {
    private String nombreFuncionario;
    private String tipoAsignacion;
    private String tipoPermiso;
    private Date fechaCreacion;
    private String tokenIdentificador;
    private String nombreRoles;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
