package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "eje_evaluacion_medica")
@Comment("Evaluacion medica asociada a ficha medica")
@EqualsAndHashCode(of = {"idEvaluacionMedica"}, callSuper = true)
public class EvaluacionMedica extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la evaluacion medica")
    private Long idEvaluacionMedica;

    @Comment("Numero de referencia de la evaluacion")
    private String numReferencia;

    @Comment("Fecha de la evaluacion")
    private Date fecha;

    @JoinColumn(name = "id_catalogo_etapa", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con etapas")
    private Catalogo etapa;

    @Comment("Talla del paciente")
    private String talla;

    @Comment("Peso del paciente")
    private String peso;

    @JoinColumn(name = "id_catalogo_tipo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con tipos de evaluacion medica")
    private Catalogo tipoEvaluacion;

    @JoinColumn(name = "id_catalogo_motivo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con motivo de la consulta")
    private Catalogo motivoConsulta;

    @Column(columnDefinition = "TEXT")
    @Comment("Recomendacion de la evaluación medica")
    private String recomendacion;

    @Comment("Detalle del lugar de atencion en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String lugarAtencion;

    @Comment("Detalla el doctor que atendio la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String doctorAtencion;

    @JoinColumn(name = "id_ficha_medica", referencedColumnName = "idFichaMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha medica del detenido a la que pertenece la evaluacion")
    private FichaMedica fichaMedica;
}
