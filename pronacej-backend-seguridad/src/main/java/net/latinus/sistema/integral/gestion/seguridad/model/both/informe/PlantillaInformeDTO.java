package net.latinus.sistema.integral.gestion.seguridad.model.both.informe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPlantillaInforme"}, callSuper = true)
public class PlantillaInformeDTO extends CamposDTO implements Serializable {
    private long idPlantillaInforme;
    private String nombre;
    private String descripcion;
    private String nemonico;
    private String nemonicoCentro;
    private String tipoCentro;
    private List<CampoInformeDTO> campos;
}
