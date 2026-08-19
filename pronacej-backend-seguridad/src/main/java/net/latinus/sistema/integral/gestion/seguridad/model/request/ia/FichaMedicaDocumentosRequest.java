package net.latinus.sistema.integral.gestion.seguridad.model.request.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorFichaMedica"}, callSuper = true)
public class FichaMedicaDocumentosRequest extends PaginacionRequest {
    private String tokenIdentificadorFichaMedica;
    private String textoBuscar;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
