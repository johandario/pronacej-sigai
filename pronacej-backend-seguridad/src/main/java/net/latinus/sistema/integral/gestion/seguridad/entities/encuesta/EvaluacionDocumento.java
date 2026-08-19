package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "enc_evaluacion_documento")
@EqualsAndHashCode(of = {"idEvaluacionDocumento"}, callSuper = true)
@Comment("Tabla de evaluaciones que relaciona con los documentos")
public class EvaluacionDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idEvaluacionDocumento;

    @JoinColumn(name = "id_catalogo_carpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del catalogo de la carpeta")
    private Catalogo catalogoCarpeta;

    @JoinColumn(name = "id_carpeta", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_encabezado", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del encabezado al que esta asociado")
    private Encabezado encabezado;

    @JoinColumn(name = "id_documento", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
