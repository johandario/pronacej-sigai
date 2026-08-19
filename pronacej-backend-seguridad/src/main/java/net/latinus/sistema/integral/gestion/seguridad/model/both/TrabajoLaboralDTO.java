package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)

public class TrabajoLaboralDTO extends CamposDTO implements Serializable {
    private Long idTrabajoLaboral;
    private Date fechaIngresoLaboral;
    private String cargoLaboral;
    private RegistroInstitucionDTO registroInstitucion;
    private String tokenFichaIdentificacion;
    private Long idFichaIdentificacion;

}