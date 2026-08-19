package net.latinus.sistema.integral.gestion.seguridad.model.request.general;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;

@Data
@EqualsAndHashCode(of = {"tokenEvaluacion"}, callSuper = true)
public class EvaluacionDocumentoRequest extends PaginacionRequest {
    private String tokenEvaluacion;
    private String nemonicoCarpeta;
}
