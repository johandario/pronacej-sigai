package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idActividadSalida"}, callSuper = true)
public class ActividadSalidaDTO extends CamposDTO {
    private Long idActividadSalida;
    private InformePermisoSalidaDTO InformePermisoSalida;
    private String descripcion;
}
