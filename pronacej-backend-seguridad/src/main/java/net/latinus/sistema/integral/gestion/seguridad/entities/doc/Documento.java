package net.latinus.sistema.integral.gestion.seguridad.entities.doc;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "doc_documento")
@EqualsAndHashCode(of = {"idDocumento"}, callSuper = true)
@Comment("Tabla que guardara información básica del documento subido en alfresco")
public class Documento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla de documentos")
    private Long idDocumento;

    @Comment("Nombre real del documento")
    private String nombreReal;

    @Comment("Tipo de archivo")
    private String mimeType;

    @Column(columnDefinition = "TEXT")
    @Comment("Id de alfresco")
    private String identificadorAlfresco;

    @Column(columnDefinition = "TEXT")
    @Comment("Descripción del documento")
    private String descripcion;

    @JoinColumn(name = "id_tipo_de_documento_sistema", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del catalogo que representa el tipo de documento del sistema")
    private Catalogo tipoDeDocumentoSistema;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa a que pertenece el documento")
    private Empresa empresa;
    
    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta que contiene el documento")
    private Carpeta carpeta;

    @Comment("Especificación del documento cuando es otro")
    private String tipoDeDocumentoSistemaOtro;

    @Comment("Tamanio en bytes del documento subido")
    private Long tamanioByteDocumento;

    public DocumentoDTO convertirADTO() {
        DocumentoDTO objetoDTO = new DocumentoDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        objetoDTO.setNombre(this.getNombreReal());
        objetoDTO.setMimeType(this.getMimeType());
        objetoDTO.setDescripcion(this.getDescripcion());
        objetoDTO.setFechaCreacion(this.getFechaCreacion());

        Catalogo tipoDeDocumentoSistema = this.tipoDeDocumentoSistema;
        if (tipoDeDocumentoSistema != null) {
            objetoDTO.setTipoDocumentoSistema(
                    tipoDeDocumentoSistema.convertirADTO()
            );
        }

        Empresa empresa1 = this.getEmpresa();
        if (empresa1 != null) {
            objetoDTO.setTokenIdentificadorEmpresa(empresa1.getTokenIdentificador());
        }

        objetoDTO.setTipoDeDocumentoSistemaOtro(this.tipoDeDocumentoSistemaOtro);
        objetoDTO.setTamanioBytes(this.tamanioByteDocumento);
        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
