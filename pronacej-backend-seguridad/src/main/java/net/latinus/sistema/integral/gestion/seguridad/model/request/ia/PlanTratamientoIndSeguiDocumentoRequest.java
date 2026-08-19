package net.latinus.sistema.integral.gestion.seguridad.model.request.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorSeguimiento"}, callSuper = true)
public class PlanTratamientoIndSeguiDocumentoRequest extends PaginacionRequest {
    private String tokenIdentificadorSeguimiento;
    private String textoBuscar;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
