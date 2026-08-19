package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class FichaDeIdentificacionDocumentoDTO extends CamposDTO {

    private DocumentoDTO documentoDTO;
    private CatalogoDTO tipoDeDocumentoFichaDeIdentificacion;

    private FichaIdentificacionDTO fichaIdentificacionDTO;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
