package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class EstudiosDTO extends CamposDTO implements Serializable {
    private Long idEstudios;
    private Date fechaInicioEstudios;
    private String cicloAcademicoActual;
    private Boolean convenioPronacej;
    private Boolean independiente;
    private RegistroInstitucionDTO registroInstitucion;
    private String tokenFichaIdentificacion;
    private Long idFichaIdentificacion;

}