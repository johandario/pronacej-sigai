package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

@Data
public class DiagnosticoDTO {
    private String tokenIdentificador;

    private String tokenIdEvaluacionMedica;

    private CatalogoDTO tipoDiagnostico;

    private String codDiagnostico;

    private String diagnostico;

    private String tratamiento;

    private String indicaciones;

    private String examenes;

    private String medicamentos;
}
