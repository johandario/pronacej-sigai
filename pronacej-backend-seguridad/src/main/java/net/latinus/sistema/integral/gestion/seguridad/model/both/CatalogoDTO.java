package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CatalogoDTO extends CamposDTO {
    private Long idCatalogo;
    private String nombre;
    private String descripcion;
    private String nemonico;
    private String codigoExterno;
    private String tokenIdentificadorPadre;
    private List<CatalogoDTO> hijos;

    private Boolean tieneHijos = false;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
