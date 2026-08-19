package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeguimientoConductualDTO extends CamposDTO {
    private Long idSeguimientoConductual;
    private String tokenEvaluacion;
    private Boolean estable;
    private Date periodoDesde;
    private Date periodoHasta;
    private String periodo;
    private String nemonicoTipoConducta;
    private String tipoConducta;
    private String descripcionConducta;
    private String accionesAdoptadas;

    private JerarquiaDTO ambiente;
    private JerarquiaDTO programa;
}
