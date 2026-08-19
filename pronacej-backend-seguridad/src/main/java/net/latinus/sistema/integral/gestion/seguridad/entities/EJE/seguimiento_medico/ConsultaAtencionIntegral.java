package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "eje_consulta_atencion_integral")
@Comment("Consulta de atencion asociada a una evaluación médica")
@EqualsAndHashCode(of = {"idConsultaAtencionIntegral"}, callSuper = true)
public class ConsultaAtencionIntegral extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la receta")
    private Long idConsultaAtencionIntegral;

    @Comment("Fecha de emisión de la receta")
    private Date fechaInicio;

    @Comment("Observaciones adicionales de la consulta atencion integral")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Motivo de la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String motivoConsulta;

    @Comment("Edad")
    @Column(columnDefinition = "TEXT")
    private String edad;

    @Comment("Tipo Enfermedad de la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String tipoEnfermedad;

    @Comment("Forma de inicio de la Enfermedad de la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String formaDeInicio;

    @Comment("Estado de animo en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String estadoDeAnimo;

    @Comment("sed")
    private Boolean sed = false;

    @Comment("sueno")
    private Boolean sueno = false;

    @Comment("apetito")
    private Boolean apetito = false;

    @Comment("Detalle de orina en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String orina;

    @Comment("Detalle de deposiciones en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String deposiciones;

    @Comment("Detalle de fiebre dentro de 15 dias en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String fiebre15dias;

    @Comment("Detalle de tos dentro de 15 dias en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String tos15dias;

    @Comment("Detalle de secrecion en genitales en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String secrecionGenitales;

    @Comment("Detalle de persida de peso en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String perdidaPeso;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla peso en kilogramos")
    private String peso;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla altura en metros")
    private String talla;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla presion en MMHG")
    private String presion;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla IMC")
    private String IMC;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalla temperatura en Centigrados")
    private String temperatura;

    @Comment("Diagnostico de la consulta atencion integral")
    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Comment("Tratamiento de la consulta atencion integral")
    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Comment("Examenes auxiliares de la consulta atencion integral")
    @Column(columnDefinition = "TEXT")
    private String examenesAuxiliares;

    @Comment("Fecha de proxima consulta atencion")
    private Date fechaProximaCita;

    @JoinColumn(name = "id_ficha_medica", referencedColumnName = "idFichaMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha medica del detenido a la que pertenece la consulta")
    private FichaMedica fichaMedica;

    @Comment("Tiempo de enfermedad de la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String tiempoEnfermedad;

    @Comment("Detalle del lugar de atencion en la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String lugarAtencion;

    @Comment("Detalla el doctor que atendio la consulta de atencion integral")
    @Column(columnDefinition = "TEXT")
    private String doctorAtencion;
}
