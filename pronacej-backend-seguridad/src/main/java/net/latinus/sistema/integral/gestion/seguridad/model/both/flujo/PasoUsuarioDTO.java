package net.latinus.sistema.integral.gestion.seguridad.model.both.flujo;


import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class PasoUsuarioDTO extends CamposDTO implements Serializable {
    private Long idPasoRol;
    private UsuarioSistemaDTO usuarioSistema;
}
