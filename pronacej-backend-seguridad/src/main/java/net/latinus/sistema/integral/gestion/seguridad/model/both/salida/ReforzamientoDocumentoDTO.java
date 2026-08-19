package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReforzamientoDocumentoDTO extends CamposDTO {
    private List<DocumentoDTO> documentoDTOList;
    private CarpetaDTO carpetaDTO;
}
