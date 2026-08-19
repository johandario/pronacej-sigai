package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class SesionReforzamientoDTO extends CamposDTO {
    private Long idSesionReforzamiento;
    private String tokenReforzamiento;
    private Date fechaSesion;
    private String nemonicoTipoSesion;
    private String nombretipoSesion;
    private String nombreResponsable;
    private String observaciones;
    private String archivo;
    private DocumentoDTO documentoDTO;
    private CarpetaDTO carpetaDTO;
}
