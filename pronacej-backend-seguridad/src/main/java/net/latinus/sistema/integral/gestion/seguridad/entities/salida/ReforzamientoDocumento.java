package net.latinus.sistema.integral.gestion.seguridad.entities.salida;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "sal_reforzamiento_documento")
@EqualsAndHashCode(of = {"idReforzamientoDocumento"}, callSuper = true)
@Comment("Tabla de informes que relaciona con los documentos")
public class ReforzamientoDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idReforzamientoDocumento;

    @JoinColumn(name = "id_carpeta", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_reforzamiento", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del reforzamiento al que esta asociado")
    private Reforzamiento reforzamiento;

    @JoinColumn(name = "id_documento", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
