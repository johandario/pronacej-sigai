package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteMatrizDelitoDTO extends CamposDTO implements Serializable {
    private Long idExpedienteDelito;
    private CatalogoDTO delitoGenerico;
    private CatalogoDTO delitoEspecifico;
    private Boolean removido;
}
