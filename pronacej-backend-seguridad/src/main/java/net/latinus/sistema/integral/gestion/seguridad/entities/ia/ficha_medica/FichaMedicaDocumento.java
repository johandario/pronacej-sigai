package net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_ficha_medica_documento")
@EqualsAndHashCode(of = {"idFichaMedicaDocumento"}, callSuper = true)
@Comment("Fichas médicas que se relacionad con documentos")
public class FichaMedicaDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idFichaMedicaDocumento;

    @JoinColumn(name = "id_carpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_documento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

    @JoinColumn(name = "id_ficha_medica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de ficha médica asociado")
    private FichaMedica fichaMedica;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
