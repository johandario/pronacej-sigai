package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class SituacionActualAdolescenteDTO extends CamposDTO implements Serializable {
    
    // Identificador de la ficha principal
    private String tokenIdentificadorFichaIdentificacion;
    
    // Identificador del tipo de área (catálogo)
    private String tokenIdentificadorTipoArea;
    
    // Identificador del tipo de situación (catálogo)
    private String tokenIdentificadorTipoSituacion;
    
    // Descripción detallada de la situación
    private String descripcion;
    
    // Observaciones adicionales
    private String observacion;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}