package net.latinus.sistema.integral.gestion.seguridad.model.request.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorDatosFamiliares"}, callSuper = true)
public class DatosFamiliaresDocumentosRequest extends PaginacionRequest {
    private String tokenIdentificadorDatosFamiliares;
    private String textoBuscar;
    private String tokenFichaIdentificacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}