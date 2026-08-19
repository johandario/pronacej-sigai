package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorEvaluacionDomiciliaria"},callSuper = true)
public class EvaluacionDomiciliariaDocumentoDTO extends CamposDTO {
    private String tokenIdentificadorEvaluacionDomiciliaria;
    private DocumentoDTO documentoDTO;
    
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}