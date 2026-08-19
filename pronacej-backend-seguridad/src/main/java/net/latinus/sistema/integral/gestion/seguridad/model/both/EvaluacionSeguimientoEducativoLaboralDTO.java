package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class EvaluacionSeguimientoEducativoLaboralDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    
    private Date fechaInicio;
    private Date fechaFin;
    
    private String tokenIdentificadorTipoEvaluacionSeguimiento;
    private String tokenIdentificadorInstitucion;
    private String tokenIdentificadorMedioVerificacion;
    private String resultadoSeguimiento;
    
    private String nombreCompletoUsuarioCreacion;
    
    private List<RecomendacionComentarioPorEvalSeguDTO> listaRecomendacionesComentarios;

    private String nombreInstitucionOtros;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}