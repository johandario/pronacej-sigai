package net.latinus.sistema.integral.gestion.seguridad.model.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaginacionFechaRequest extends PaginacionRequest implements Serializable {

    private Date fechaInicio;
    private Date fechaFin;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
