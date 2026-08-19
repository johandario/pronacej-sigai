package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class FichaIdentificacionTipoDeDocumentoDTO extends CamposDTO {

    private CatalogoDTO seccionFichaDeIdentificacionDTO;
    private CatalogoDTO tipoArchivoSistemaDTO;
    private Boolean requerido;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
