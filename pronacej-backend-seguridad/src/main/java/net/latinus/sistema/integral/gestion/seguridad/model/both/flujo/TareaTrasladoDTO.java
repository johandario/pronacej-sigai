package net.latinus.sistema.integral.gestion.seguridad.model.both.flujo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaTrasladoDTO {
    public TrasladoDTO traslado;
    public TareaDTO tarea;
}
