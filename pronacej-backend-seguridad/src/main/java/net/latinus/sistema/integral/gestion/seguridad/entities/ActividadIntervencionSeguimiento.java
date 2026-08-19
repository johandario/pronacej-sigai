package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_actividad_intervencion_seguimiento")
@Comment("Tabla dependiente Actividad Intervencion, donde se definen sus seguimientos")
@EqualsAndHashCode(of = {"idActividadIntervencionSeguimiento"}, callSuper = true)
public class ActividadIntervencionSeguimiento extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idActividadIntervencionSeguimiento;

    @ManyToOne
    @JoinColumn(name = "id_plan_interv_actividad")
    @Comment("Actvidad o programa al que se encuentra vinculado")
    private ActividadIntervencion actividad;

    @Comment("Fecha de planificación")
    private Date fecha;

    @Comment("Hora en la que se inicia")
    private String horaInicio;

    @Comment("Hora en la que se finaliza")
    private String horaFin;

    @Comment("Observaciones de seguimiento")
    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
