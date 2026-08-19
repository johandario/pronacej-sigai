package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_sancion_disciplinaria_documento")
@EqualsAndHashCode(of = {"idSancionDisciplinariaDocumento"}, callSuper = true)
@Comment("Tabla de seguimiento educativo laboral que relaciona con los documentos")
public class SancionDisciplinariaDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idSancionDisciplinariaDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_sancion_disciplinaria", referencedColumnName = "idSancionDisciplinaria")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la sancion")
    private SancionDisciplinaria sancionDisciplinaria;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

}
