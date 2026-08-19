package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
public class MedicamentoDTO {
    private String codigo;
    private String nombre;
    private String presentacion;
    private String concentracion;
    private String tokenIdentificador;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}