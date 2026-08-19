package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_acta_externamiento_documento")
@EqualsAndHashCode(of = {"idActaExternamientoDocumento"}, callSuper = true)
@Comment("Tabla de acta de externamiento que relaciona con los documentos")
public class ActaExternamientoDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idActaExternamientoDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_acta_externamiento", referencedColumnName = "id_acta_externamiento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del acta de externamiento a la que esta asociado")
    private ActaExternamiento actaExternamiento;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
