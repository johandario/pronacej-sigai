package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrientacionConsejeriaFamiliarDTO extends CamposDTO implements Serializable {
    
    private Date fecha;
    private String descripcion;
    private String tokenIdentificadorPersonaRelacionada;
    private String nombreCompletoUsuarioCreacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}