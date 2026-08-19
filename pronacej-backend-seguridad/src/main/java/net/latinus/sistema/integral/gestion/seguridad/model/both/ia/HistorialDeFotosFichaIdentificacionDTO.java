package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class HistorialDeFotosFichaIdentificacionDTO extends CamposDTO {

    private CatalogoDTO tipo;

    private FichaIdentificacionDTO fichaIdentificacionDTO;

    private DocumentoDTO documentoDTO;
    private CarpetaDTO carpetaDTO;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
