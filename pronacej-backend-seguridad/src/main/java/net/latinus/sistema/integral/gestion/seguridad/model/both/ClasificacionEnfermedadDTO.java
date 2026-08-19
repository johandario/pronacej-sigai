package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
public class ClasificacionEnfermedadDTO {
    private String codigo;
    private String nombre;
    private String tokenIdentificador;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
