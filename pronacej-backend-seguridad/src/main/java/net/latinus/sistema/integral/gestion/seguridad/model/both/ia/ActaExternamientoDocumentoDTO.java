package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActaExternamientoDocumentoDTO extends CamposDTO {
    private DocumentoDTO documentoDTO;
    private CarpetaDTO carpetaDTO;
}
