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
@EqualsAndHashCode(callSuper = true)
public class PlantillaVariableDTO extends CamposDTO {

    private String clave;
    private String nombre;
    private String valor;
    private Integer orden;
    private String tokenIdentificadorPlantillaFormulario;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
