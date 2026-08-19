package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeguimientoPsicologicoDTO extends CamposDTO {
    private Long idSeguimientoPsicologico;
    private String tokenEvaluacion;
    private String intervencionConcejeria;
    private String accionesRealizar;
    private String comentariosObservaciones;

    private JerarquiaDTO ambiente;
    private JerarquiaDTO programa;
}
