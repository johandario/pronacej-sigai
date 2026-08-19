package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
public class EspecialidadProductoDTO {
    private String especialidad;
    private String producto;
    private String tipoProducto;
    private String tokenIdentificador;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}