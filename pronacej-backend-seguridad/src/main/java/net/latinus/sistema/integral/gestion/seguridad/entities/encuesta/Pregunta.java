package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "enc_pregunta")
@Comment("Tabla de Preguntas de la Encuesta")
@EqualsAndHashCode(of = {"idPregunta"}, callSuper = true)
public class Pregunta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador único de la pregunta")
    private Long idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion", nullable = false)
    @Comment("Referencia a la sección de la encuesta")
    private Seccion seccion;

    @Column(name = "texto", length = 1024, nullable = false)
    @Comment("Texto de la pregunta")
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    @Comment("Referencia al catalogo de tipo de pregunta")
    private Catalogo categoria;

    @Column(name = "orden")
    @Comment("Orden de la pregunta dentro de la sección")
    private Integer orden;

    @Column(name = "requerido", nullable = false)
    @Comment("Check para saber si la pregunta es obligatoria")
    private Boolean requerido;

    @Column(name = "respuestas_ordenadas", nullable = false)
    @Comment("Check para saber si las respuestas tienen orden")
    private Boolean respuestasOrdenadas;

    @Column(name = "tiene_observaciones", nullable = false)
    @Comment("Check para saber si la pregunta tiene campo observaciones")
    private Boolean tieneObservaciones;

    @Column(name = "permite_documentos", nullable = false)
    @Comment("Check para saber si la pregunta permite subir documentos")
    private Boolean permiteDocumentos;
}
