package net.latinus.sistema.integral.gestion.seguridad.model.both.flujo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idInstanciaProceso"}, callSuper = true)
public class InstanciaProcesoDTO extends CamposDTO implements Serializable {
    private Long idInstanciaProceso;
    private String estado;
    private String descripcion;
    private ProcesoDTO proceso;
    private List<TareaDTO> tareas;
}
