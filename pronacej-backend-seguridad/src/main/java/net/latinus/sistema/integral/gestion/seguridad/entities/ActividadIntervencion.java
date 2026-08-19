package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_actividad_intervencion")
@Comment("Tabla dependiente Intervencion Diferenciada, donde se definen sub actividades")
@EqualsAndHashCode(of = {"idActividadIntervencion"}, callSuper = true)
public class ActividadIntervencion extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idActividadIntervencion;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_frecuencia")
    @Comment("Frecuencia")
    private Catalogo frecuencia;

    @Comment("Subactividad")
    @Column(columnDefinition = "TEXT")
    private String subactividad;

    @Comment("Fecha de Inicio")
    private Date fechaInicio;

    @Comment("Fecha de edicion")
    private Date fechaFin;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_ind_interv")
    @Comment("Intervencion")
    private PlanTratamientoIndInterv planTratamientoIndInterv;
}
