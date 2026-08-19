package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "enc_encuesta")
@Comment("Tabla de Encuestas")
@EqualsAndHashCode(of = {"idEncuesta"}, callSuper = true)
public class Encuesta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encuesta")
    @Comment("Identificador único de la encuesta")
    private Long idEncuesta;

    @Column(name = "nombre", nullable = false)
    @Comment("Nombre de la encuesta")
    private String nombre;

    @Column(name = "descripcion")
    @Comment("Descripcion de la encuesta")
    private String descripcion;

    @Column(name = "secciones_ordenadas", nullable = false)
    @Comment("Check para saber si las secciones tienen orden")
    private Boolean seccionesOrdenadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jerarquia")
    @Comment("Referencia a la jerarquía del cuestionario")
    private Jerarquia jerarquia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = false)
    @Comment("Referencia al catalogo de la encuesta")
    private Catalogo catalogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_centro", nullable = false)
    @Comment("Referencia al catalogo de tipo de centro")
    private Catalogo tipoCentro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria")
    @Comment("Referencia al catalogo de categoria de la encuesta")
    private Catalogo categoria;

    @Column(name = "version")
    @Comment("Versión de la evaluacion")
    private Integer version = 1;
}
