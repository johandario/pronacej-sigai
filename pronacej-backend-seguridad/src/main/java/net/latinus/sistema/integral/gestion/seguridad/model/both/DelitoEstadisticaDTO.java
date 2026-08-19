package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.Serializable;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@AllArgsConstructor
public class DelitoEstadisticaDTO implements Serializable {

    private String nombreDelito;
    private Integer cantidad;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
