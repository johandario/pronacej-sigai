package net.latinus.sistema.integral.gestion.seguridad.model.response;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;

import java.util.List;

@Data
public class NavigationFuseResponse {

    private List<MenuDTO> porDefecto;
    private List<MenuDTO> compact;

}
