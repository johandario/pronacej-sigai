package net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ai_ficha_medica")
@Comment("Ficha médica creada al ingresar al detenido")
@EqualsAndHashCode(of = {"idFichaMedica"}, callSuper = true)
public class FichaMedica extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la ficha médica")
    private Long idFichaMedica;

    @JoinColumn(name = "id_catalogo_tipo_sangre", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con tipos de sangre")
    private Catalogo tipoSangre;

    @Column(columnDefinition = "TEXT")
    @Comment("Estado de salud")
    private String estadoSalud;

    @Column(columnDefinition = "TEXT")
    @Comment("Lesiones del detenido")
    private String lesiones;

    @Column(columnDefinition = "TEXT")
    @Comment("Enfermedades del detenido")
    private String enfermedades;

    @Column(columnDefinition = "TEXT")
    @Comment("Medicamentos del detenido")
    private String medicamentos;

    @Comment("Nombre del seguro médico del detenido")
    private String seguroMedico;

    @Column(columnDefinition = "TEXT")
    @Comment("Institución a la que acude cuando se encuentra enfermo")
    private String institucionAcude;

    @Column(columnDefinition = "TEXT")
    @Comment("Nombre del hospital donde ha sido internado y el motivo")
    private String internadoHospital;

    @Comment("Atributo para conocer si tiene alergias a algun alimento")
    private Boolean alergiaAlimentos;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle de los alimentos a los que es alergico")
    private String detalleAlergiasAlimentos;

    @Comment("Atributo para concer si tiene alergias a algun medicamento")
    private Boolean alergiaMedicamentos;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle de los medicamentos a los que es alergico")
    private String medicamentosAlergicos;

    @Comment("Atributo para conocer si se ha realizado alguna cirugia")
    private Boolean cirugiaQuirurgica;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle las cirugias que se ha realizado")
    private String detalleCirugias;

    @Comment("Atributo para conocer si ha tenido fracturas")
    private Boolean fracturas;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle las fracturas que ha tenido")
    private String detalleFracturas;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle las Infecciones Respiratorias Superiores")
    private String IRS;

    @Comment("Atributo para conocer si ha hecho uso de preservativos")
    private Boolean usoDePreservativo;

    @JoinColumn(name = "tipo_genero", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Atributo para conocer el tipo de relacion que lleva")
    private Catalogo genero;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla si hace uso de un desfribilador")
    private String ICD;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla si tiene una droga de inicio")
    private String drogaInicio;

    @Comment("Atributo para conocer si tiene habitos nocivos")
    private Boolean habitosNocivos;

    @Comment("Atributo para conocer si tiene consume alcohol")
    private Boolean tomaAlcohol;

    @Comment("Atributo para conocer si tiene consume tabaco")
    private Boolean tabaco;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla desde que edad consume alcohol")
    private String edadAlcohol;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla desde que edad consume tabaco")
    private String edadTabaco;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla peso en kilogramos")
    private String peso;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla altura en metros")
    private String talla;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla un aspecto general fisico del adolescente")
    private String aspectoGeneralFisico;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla una inspeccion fisica del adolescente")
    private String inspeccion;

    @Column(columnDefinition = "TEXT")
    @Comment("Atributo para detallar la piel y faneras del adolescente")
    private String pielFaneras;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla presion en MMHG")
    private String presion;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla saturacion del oxigeno en sangre")
    private String saturacionOxigeno;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla el indice de masa corporal")
    private String indiceMasaCorporal;

    @Column(name = "cabeza_detalle", columnDefinition = "TEXT")
    private String cabezaDetalle;

    @Column(name = "ojos_detalle", columnDefinition = "TEXT")
    private String ojosDetalle;

    @Column(name = "oido_detalle", columnDefinition = "TEXT")
    private String oidoDetalle;

    @Column(name = "nariz_detalle", columnDefinition = "TEXT")
    private String narizDetalle;

    @Column(name = "boca_detalle", columnDefinition = "TEXT")
    private String bocaDetalle;

    @Column(name = "orofaringe_detalle", columnDefinition = "TEXT")
    private String orofaringeDetalle;

    @Column(name = "corazon_detalle", columnDefinition = "TEXT")
    private String corazonDetalle;

    @Column(name = "pulmones_detalle", columnDefinition = "TEXT")
    private String pulmonesDetalle;

    @Column(name = "abdomen_detalle", columnDefinition = "TEXT")
    private String abdomenDetalle;

    @Column(name = "urinario_detalle", columnDefinition = "TEXT")
    private String urinarioDetalle;

    @Column(name = "ppl_detalle", columnDefinition = "TEXT")
    private String pplDetalle;

    @Column(name = "pru_detalle", columnDefinition = "TEXT")
    private String pruDetalle;

    @Column(name = "impresion_diagnostico", columnDefinition = "TEXT")
    private String impresionDiagnostico;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha de identificación del detenido a la que pertenece la ficha médica")
    private FichaIdentificacion fichaIdentificacion;
}
