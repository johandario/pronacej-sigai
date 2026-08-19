package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Data
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class JerarquiaDTO extends CamposDTO implements Serializable {

    private Long id;
    private Long idJerarquiaPadre;
    private JerarquiaDTO jerarquiaPadre;
    private String nombre;
    private String nemonico;    
    private String ubigeo;
    private Long empresa;
    private String direccion;
    private String tokenIdentificadorGenero;
    private CatalogoDTO genero;
    private Boolean esOficinaCentral;
    private String nemonicoPadre;
    private List<JerarquiaDTO> hijos = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(JerarquiaDTO.class.getName());

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(
                    EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            return "";
        }
    }
}
