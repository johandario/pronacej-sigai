package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "enc_respuesta")
@Comment("Tabla de Respuestas de Preguntas")
@EqualsAndHashCode(of = {"idRespuesta"}, callSuper = true)
public class Respuesta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    @Comment("Identificador único de la respuesta")
    private Long idRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    @Comment("Referencia a la pregunta a la que está asociada la respuesta")
    private Pregunta pregunta;

    @Column(name = "respuesta", nullable = false, columnDefinition = "TEXT")
    @Comment("Texto de la respuesta")
    private String respuesta;

    @Column(name = "valor_respuesta")
    @Comment("Peso asignado a la respuesta")
    private Long valorRespuesta;

    @Column(name = "orden")
    @Comment("Orden de la respuesta")
    private Integer orden;

    @Column(name = "respuesta_correcta")
    @Comment("Indica si la respuesta es correcta")
    private Boolean respuestaCorrecta;
}
