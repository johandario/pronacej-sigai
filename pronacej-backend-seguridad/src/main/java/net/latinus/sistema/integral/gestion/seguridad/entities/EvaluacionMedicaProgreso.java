package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "eje_evaluacion_medica_progreso")
@Comment("Evaluacion de progreso asociada a la ficha medica")
@EqualsAndHashCode(of = {"idEvaluacionMedicaProgreso"}, callSuper = true)
public class EvaluacionMedicaProgreso extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la evaluacion medica progreso")
    private Long idEvaluacionMedicaProgreso;

    @JoinColumn(name = "id_tipo_evaluacion_progreso", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con los tipos de evaluacion")
    private Catalogo tipoEvaluacionProgreso;

    @JoinColumn(name = "id_estado_nutricional", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con los estados nutricional")
    private Catalogo estadoNutricional;

    @JoinColumn(name = "id_tipo_desnutricion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con los estados nutricional")
    private Catalogo tipoDesnutricion;

    @Comment("Grado de Nutricion")
    @Column(columnDefinition = "TEXT")
    private String grado;

    @Comment("clinicamente sano")
    private Boolean clinicamenteSano = false;

    @Comment("enfermo")
    private Boolean enfermo = false;

    @Comment("Fecha de la evaluacion")
    private Date fecha;

    @Comment("peso")
    @Column(columnDefinition = "TEXT")
    private String peso;

    @Comment("talla")
    @Column(columnDefinition = "TEXT")
    private String talla;

    @Comment("imc")
    @Column(columnDefinition = "TEXT")
    private String imc;

    @Comment("impresion diagnostico")
    @Column(columnDefinition = "TEXT")
    private String impresionDiagnostico;

    @Comment("manejo terapeutico")
    @Column(columnDefinition = "TEXT")
    private String manejoTerapeutico;

    @JoinColumn(name = "id_ficha_medica", referencedColumnName = "idFichaMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha medica del detenido a la que pertenece la evaluacion")
    private FichaMedica fichaMedica;
}
