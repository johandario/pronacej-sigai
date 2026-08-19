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
import java.util.ArrayList;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorFichaAsistenciaPostEgreso"},callSuper = true)
public class FichaAsistenciaPostEgresoDocumentoDTO extends CamposDTO {
    private String tokenIdentificadorFichaAsistenciaPostEgreso;
    private DocumentoDTO documentoDTO;

    private String nombre;
    private String nemonico;
    private String tipoLocalidad;
    private String rutaUbigeo;

    private Boolean tieneHijos = false;

    private String ubigeo;

    private ArrayList<LocalidadDTO> hijos = new ArrayList<>();

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
