package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeguimientoEducativoLaboralOtrosDTO extends CamposDTO implements Serializable {
    
    // Identificadores
    private String tokenEvaluacionSeguimiento;
    
    // Información de la institución
    private String institucionVisitada;
    private String personaEntrevistada;
    private String direccion;
    
    // Información del seguimiento
    private Date fechaSeguimiento;
    private String medioVerificacion;
    private String resultadoSeguimiento;
    private String sugerenciasRecomendaciones;
    
    // Relaciones con otras entidades
    private JerarquiaDTO programa;
    private JerarquiaDTO ambiente;
    private JerarquiaDTO centro;
    private String tokenIdentificadorTipoSeguimientoSocial;
    
    // Campos adicionales para interfaz de usuario
    private String nombreCompletoUsuarioCreacion;

    private String tokenFichaIdentificacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
