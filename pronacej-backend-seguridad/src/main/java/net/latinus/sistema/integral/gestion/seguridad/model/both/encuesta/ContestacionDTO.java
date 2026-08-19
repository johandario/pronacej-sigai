package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idContestacion"}, callSuper = true)
public class ContestacionDTO extends CamposDTO {
    private Long idContestacion;
    private Long idPregunta;
    private Long idRespuesta;
    private String contestacion;
    private String observacion;
}
