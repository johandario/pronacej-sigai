package net.latinus.sistema.integral.gestion.seguridad.model.response.doc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContenidoCarpetaResponse extends RutaContenidoCarpetaResponse{

    private String tokenIdentificadorDocumento;
    private String tokenIdentificadorCarpeta;

    private String descripcion;

    private Date fechaDeCreacion;
    private UsuarioSistemaDTO usuarioQueCreo;
    private Long sizeBytes;
    private String tipo = "carpeta";

    private Long cantidadDeDocumentos;
    private Long cantidadDeCarpetas;

    private List<RutaContenidoCarpetaResponse> rutaContenidoCarpetaResponseList;

    private List<ContenidoCarpetaResponse> documentos;
    private List<ContenidoCarpetaResponse> carpetas;

}
