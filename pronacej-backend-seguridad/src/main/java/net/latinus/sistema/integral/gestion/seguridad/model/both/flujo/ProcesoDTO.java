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
@EqualsAndHashCode(of = {"idProceso"}, callSuper = true)
public class ProcesoDTO extends CamposDTO implements Serializable {
    private Long idProceso;
    private String nombre;
    private Integer version;
    private String nemonico;
    private String fecCreacion;
    private List<PasoDTO> pasos;
}


