package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class RelacionEgresoDTO extends CamposDTO implements Serializable {
    private String numExpediente;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String tipoDocumento;
    private String numDocumento;
    private String tokenExpediente;
    private String tokenFichaIdentificacion;
}
