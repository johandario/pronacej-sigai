package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteMatrizMedidaDTO extends CamposDTO {
    private Long idExpedienteMedida;
    private CatalogoDTO medida;
    private Boolean removido;
}
