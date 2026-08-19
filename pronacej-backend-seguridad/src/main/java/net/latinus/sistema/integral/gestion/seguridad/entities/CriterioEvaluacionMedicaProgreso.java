package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.EvaluacionMedica;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "eje_criterio_evaluacion_medica_progreso")
@Comment("Criterios Asociados a la Evaluacion de Progreso")
@EqualsAndHashCode(of = {"idCriterioEvaluacionMedicaProgreso"}, callSuper = true)
public class CriterioEvaluacionMedicaProgreso extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del criterio de la evaluacion medica de progreso")
    private Long idCriterioEvaluacionMedicaProgreso;

    @JoinColumn(name = "id_signo_alteracion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo del tipo de signo o alteracion")
    private Catalogo tipoSignoAlteracion;

    @JoinColumn(name = "id_signo_alteracion_hijo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo del tipo de signo o alteracion hijo")
    private Catalogo tipoSignoAlteracionHijo;

    @JoinColumn(name = "id_ubicacion_signo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo de la ubicacion del signo o alteracion")
    private Catalogo ubicacionSigno;

    @JoinColumn(name = "id_lado_signo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo del lado del signo o alteracion")
    private Catalogo ladoSigno;

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    @Comment("Atributo para determinar si se encuentra presente un signo o alteracion")
    private Boolean presente;

    @JoinColumn(name = "id_evaluacion_medica_progreso", referencedColumnName = "idEvaluacionMedicaProgreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Evaluacion medica de progreso del detenido")
    private EvaluacionMedicaProgreso evaluacionMedicaProgreso;

}
