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
public class CarpetaDTO extends CamposDTO {

    private String nombreCliente;
    private String nombreAlfresco;
    private String descripcion;

    private CarpetaDTO carpetaDTOPadre;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
