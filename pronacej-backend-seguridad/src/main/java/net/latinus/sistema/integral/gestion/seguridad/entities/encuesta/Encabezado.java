package net.latinus.sistema.integral.gestion.seguridad.entities.encuesta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "enc_encabezado")
@Comment("Tabla que guarda un resumen de la encuesta")
@EqualsAndHashCode(of = {"idEncabezado"}, callSuper = true)
public class Encabezado extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encabezado")
    @Comment("Identificador único del encabezado")
    private Long idEncabezado;

    @Column(name = "nombre")
    @Comment("Nombre de la encuesta")
    private String nombre;

    @Column(name = "descripcion")
    @Comment("Descripcion de la encuesta")
    private String descripcion;

    @Column(name = "valor_total")
    @Comment("Referencia al puntaje total de la encuesta")
    private Double valorTotal;

    @Column(name = "completado")
    @Comment("Si la evaluacion esta completada o no")
    private Boolean completada;

    @Column(name = "fecha_completacion")
    @Comment("Fecha en la que se completa la encuesta")
    private Date fechaCompletacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encuesta")
    @Comment("Referencia a la encuesta")
    private Encuesta encuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Referencia a la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_valoracion_final", referencedColumnName = "idCatalogo")
    @Comment("Nivel de riesgo final SAVRY (catálogo NIVEL_RIESGO)")
    private Catalogo valoracionFinal;

    @Column(name = "justificacion_valoracion", columnDefinition = "TEXT")
    @Comment("Justificación de la valoración final SAVRY")
    private String justificacionValoracion;

    @Column(name = "fecha_valoracion")
    @Comment("Fecha/hora de la valoración final SAVRY")
    private Date fechaValoracion;

}
