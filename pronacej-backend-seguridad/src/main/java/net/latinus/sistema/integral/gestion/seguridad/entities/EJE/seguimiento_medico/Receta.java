package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "eje_receta")
@Comment("Receta asociada a una evaluación médica")
@EqualsAndHashCode(of = {"idReceta"}, callSuper = true)
public class Receta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la receta")
    private Long idReceta;

    @JoinColumn(name = "id_evaluacion_medica", referencedColumnName = "idEvaluacionMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Evaluación Médica asociada a la receta")
    private EvaluacionMedica evaluacionMedica;

//    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Comment("Lista de detalles de la receta")
//    private List<DetalleReceta> detalles = new ArrayList<>();

    @Comment("Fecha de emisión de la receta")
    private Date fechaEmision;

    @Comment("Observaciones adicionales de la receta")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Número de la receta")
    private String numeroReceta;

    @JoinColumn(name = "id_tipo_especialidad", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con la especialidad médica asociada a la receta")
    private Catalogo especialidad;

    @JoinColumn(name = "id_consulta_medica_integral", referencedColumnName = "idConsultaAtencionIntegral")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Consulta Médica Integral asociada a la receta")
    private ConsultaAtencionIntegral consultaAtencionIntegral;
}