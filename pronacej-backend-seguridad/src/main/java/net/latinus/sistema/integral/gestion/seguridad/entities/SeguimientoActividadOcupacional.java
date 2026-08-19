package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_seguimiento_actividad_ocupacional")
@Comment("Seguimiento de la actividad ocupacional")
@EqualsAndHashCode(of = {"idSeguimientoActividadOcupacional"}, callSuper = true)
public class SeguimientoActividadOcupacional extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idSeguimientoActividadOcupacional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad_ocupacional", referencedColumnName = "idActividadOcupacional")
    @Comment("Actividad ocupacional asociada")
    private ActividadOcupacional actividadOcupacional;

    @Comment("Descripción de la actividad realizada")
    @Column(columnDefinition = "TEXT")
    private String actividad;

    @Comment("Estado de vigencia del seguimiento")
    private Boolean vigente;

    @Comment("Observaciones adicionales")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Fecha en que se realizó la actividad")
    private Date fechaActividad;
}