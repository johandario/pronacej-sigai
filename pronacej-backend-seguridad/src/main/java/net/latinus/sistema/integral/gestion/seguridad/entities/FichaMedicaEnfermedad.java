package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_ficha_medica_enfermedad")
@Comment("Enfermedad relacionada con la ficha identificacion")
@EqualsAndHashCode(of = {"idFichaMedicaEnfermedad"}, callSuper = true)
public class FichaMedicaEnfermedad extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la enfermedad relacionada a la ficha identificacion")
    private Long idFichaMedicaEnfermedad;

    @ManyToOne
    @JoinColumn(name = "id_ficha_medica", referencedColumnName = "idFichaMedica")
    @Comment("Id de la ficha médica")
    private FichaMedica idFichaMedica;

    @ManyToOne
    @JoinColumn(name = "id_tipo_enfermedad", referencedColumnName = "idCatalogo")
    private Catalogo tipoEnfermedad;

    @ManyToOne
    @JoinColumn(name = "id_clasificacion_enfermedad")
    private ClasificacionEnfermedad clasificacionEnfermedad;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle de la enfermedad")
    private String detalle;

    @Comment("Se detalla si la enfermedad se encuentra presente")
    private Boolean enfermedadActual;

    @Comment("Se detalla la edad en que se presento la enfermedad")
    private String edadPresente;

    @Column(columnDefinition = "TEXT")
    @Comment("Tratamiento de la enfermedad")
    private String tratamiento;

    @Comment("Fecha de aparicion enfermdad")
    private Date fechaAparicion;
}
