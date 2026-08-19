package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "enc_seccion")
@Comment("Tabla de Secciones de Encuesta")
@EqualsAndHashCode(of = {"idSeccion"}, callSuper = true)
public class Seccion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seccion")
    @Comment("Identificador único de la seccion")
    private Long idSeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encuesta", nullable = false)
    @Comment("Referencia a la encuesta")
    private Encuesta encuesta;

    @Column(name = "nombre", nullable = false, length = 1024)
    @Comment("Nombre de la seccion")
    private String nombre;

    @Column(name = "orden")
    @Comment("Orden de la seccion dentro de la encuesta")
    private Integer orden;

    @Column(name = "preguntas_ordenadas", nullable = false)
    @Comment("Check para saber si las preguntas tienen orden")
    private Boolean preguntasOrdenadas = true;

    @Column(name = "tiene_puntuacion")
    @Comment("Check para saber si la seccion tiene puntuacion")
    private Boolean tienePuntuacion = false;
}
