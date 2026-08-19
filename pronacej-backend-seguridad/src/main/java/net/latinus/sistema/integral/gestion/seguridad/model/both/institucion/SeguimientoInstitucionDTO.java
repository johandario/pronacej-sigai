package net.latinus.sistema.integral.gestion.seguridad.model.both.institucion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idSeguimientoInstitucion"}, callSuper = true)
public class SeguimientoInstitucionDTO extends CamposDTO{
    private Long idSeguimientoInstitucion;
    private Date fechaRegistro;
    private String numeroDoc;
    private String estado;
    private Date fecha;
    private String personaEntrevistada;
    private String fortalezas;
    private String debilidades;
    private Boolean cumpleObjetivo;
    private String personaResponsable;
    private RegistroInstitucionDTO registroInstitucion;


}
