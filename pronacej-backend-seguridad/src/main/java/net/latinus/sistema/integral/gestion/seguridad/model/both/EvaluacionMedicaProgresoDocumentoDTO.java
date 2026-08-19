package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorEvaluacionMedicaProgreso"},callSuper = true)
public class EvaluacionMedicaProgresoDocumentoDTO extends CamposDTO {

    private String tokenIdentificadorEvaluacionMedicaProgreso;
    private DocumentoDTO documentoDTO;

    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}