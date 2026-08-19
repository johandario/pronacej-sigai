package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "enc_contestacion")
@Comment("Tabla de respuestas de usuarios a las evaluaciones")
@EqualsAndHashCode(of = {"idContestacion"}, callSuper = true)
public class Contestacion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contestacion")
    @Comment("Identificador único de la contestacion")
    private Long idContestacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encabezado", nullable = false)
    @Comment("Referencia al encabezado de la evaluacion")
    private Encabezado encabezado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    @Comment("Referencia a la pregunta que se está contestando")
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_respuesta")
    @Comment("Referencia a la opcion de la pregunta")
    private Respuesta respuesta;

    @Column(name = "contestacion", columnDefinition = "TEXT")
    @Comment("Texto de respuesta a la pregunta")
    private String contestacion;

    @Column(name = "observacion", columnDefinition = "TEXT")
    @Comment("Texto de observaciones")
    private String observacion;
}
