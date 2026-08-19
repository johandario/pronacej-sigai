package net.latinus.sistema.integral.gestion.seguridad.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class HistoricoEntradaSalidaRequest extends CamposDTO {

    private CatalogoDTO tipoEntrada;
}
