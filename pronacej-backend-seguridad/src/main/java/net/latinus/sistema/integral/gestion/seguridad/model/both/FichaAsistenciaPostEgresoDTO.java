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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FichaAsistenciaPostEgresoDTO extends CamposDTO implements Serializable {

    private CatalogoDTO tipoFormato;

    private String tokenIdentificadorFichaIdentificacion;

    private String tokenPlanAsistencia;

    private List<DetalleFichaAsistenciaPostEgresoDTO> detalleFichaAsistenciaPostEgresos;

    private PlanAsistenciaPostEgresoDetalleDTO planAsistenciaPostEgresoDetalle;

    private Long idFichaIdentificacion;

    private Long idPlanAsistenciaPostEgreso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
