package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.Serializable;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class DetalleFichaAsistenciaPostEgresoDTO extends CamposDTO implements Serializable {
    private Long idDetalleFichaAsistenciaPostEgreso;
    private Date fechaDetalle;
    private String descripcionActividad;
    private String observaciones;

    private CatalogoDTO modalidadDeEntrevista;
    private CatalogoDTO personaEntrevistada;
    private CatalogoDTO motivo;

    private String tokenIdentificadorFichaAsistenciaPostEgreso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
