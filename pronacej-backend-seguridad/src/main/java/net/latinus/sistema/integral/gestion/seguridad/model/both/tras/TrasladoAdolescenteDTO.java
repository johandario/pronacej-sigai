package net.latinus.sistema.integral.gestion.seguridad.model.both.tras;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idTrasladoAdolescente"}, callSuper = true)
public class TrasladoAdolescenteDTO extends CamposDTO {
    private Long idTrasladoAdolescente;
    private FichaIdentificacionDTO fichaIdentificacion;
    private Boolean isComplete;
    private CatalogoDTO estadoEvento;
    private Boolean completado;
}
