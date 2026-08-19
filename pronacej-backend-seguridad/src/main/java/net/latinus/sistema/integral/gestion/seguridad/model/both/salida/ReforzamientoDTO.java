package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReforzamientoDTO extends CamposDTO implements Serializable {
    private Long idReforzamiento;
    private Boolean planVida;
    private String tokenFichaIdentificacion;
    private Long idFichaIdentificacion;
    private Integer numeroSesiones;
    private Date fechaUltimaSesion;
    private String tipoUltimaSesion;
    private String responsableUltimaSesion;
    private String observacionesUltimaSesion;
    private String fechaCreacionFormateada;
    private String fechaUltimaSesionFormateada;
    private List<SesionReforzamientoDTO> sesiones;
    private ReforzamientoDocumentoDTO reforzamientoDocumentoDTO;
}
