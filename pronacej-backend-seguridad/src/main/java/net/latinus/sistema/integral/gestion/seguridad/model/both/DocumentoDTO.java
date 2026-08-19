package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.Serializable;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentoDTO extends CamposDTO implements Serializable {

    private String mimeType;

    private String nombre;
    private Long tamanioBytes;

    private CatalogoDTO tipoDocumentoSistema;
    private String descripcion;

    private String tipoDeDocumentoSistemaOtro;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
