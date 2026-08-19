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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ParametroDelSistemaDTO extends CatalogoDTO {

    private String valor;
    private String valorExterno;

    private List<ParametroDelSistemaDTO> hijos2;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
