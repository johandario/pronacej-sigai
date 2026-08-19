package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlantillaFormularioDTO extends CamposDTO {

    private String formularioString;
    private String contenidoHtml;
    private String nemonico;
    private String descripcion;
    private String razon;
    private String tokenIdentificadorFormularioRelacionado;

    private List<PlantillaVariableDTO> listaVariables;
    private List<PlantillaVariableDTO> listaVariablesEliminar;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
