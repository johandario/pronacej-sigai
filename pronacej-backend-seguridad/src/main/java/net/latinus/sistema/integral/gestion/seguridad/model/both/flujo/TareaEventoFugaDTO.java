package net.latinus.sistema.integral.gestion.seguridad.model.both.flujo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TareaEventoFugaDTO {
    public EventoFugaDTO eventoFuga;
    public TareaDTO tarea;
}
