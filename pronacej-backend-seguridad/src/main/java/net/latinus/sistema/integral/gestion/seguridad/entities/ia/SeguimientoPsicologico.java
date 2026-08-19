package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Encabezado;

import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_seguimiento_psicologico")
@EqualsAndHashCode(of = {"idSeguimientoPsicologico"}, callSuper = true)
@Comment("Tabla de seguimientos psicologicos")
public class SeguimientoPsicologico extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento_psicologico")
    @Comment("Id del seguimiento psicologico")
    private Long idSeguimientoPsicologico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    @Comment("Referencia a evaluacion realizada")
    private Encabezado evaluacion;

    @Comment("Descripcion de la intervencion")
    @Column(name = "intervencion_concejeria", nullable = false,columnDefinition = "TEXT")
    private String intervencionConcejeria;

    @Comment("Descripcion de las acciones a realizar")
    @Column(name = "acciones_realizar", nullable = false,columnDefinition = "TEXT")
    private String accionesRealizar;

    @Comment("Comentarios y/o Observaciones")
    @Column(name = "comentarios_observaciones",columnDefinition = "TEXT")
    private String comentariosObservaciones;

    @ManyToOne
    @JoinColumn(name = "id_programa_jerarquiera")
    @Comment("Programa Actividad Ocupacional")
    private Jerarquia programa;

    @ManyToOne
    @JoinColumn(name = "id_ambiente_jerarquiera")
    @Comment("Ambiente Actividad Ocupacional")
    private Jerarquia ambiente;
}
