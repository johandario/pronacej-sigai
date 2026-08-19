package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "eje_orden_medica")
@Comment("Orden asociada a una evaluación médica")
@EqualsAndHashCode(of = {"idOrdenMedica"}, callSuper = true)
public class OrdenMedica extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la orden")
    private Long idOrdenMedica;

    @Comment("Fecha de emisión de la orden")
    private Date fechaEmision;

    @Comment("Observaciones adicionales de la orden")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Número de la orden")
    private String numeroOrden;

    @JoinColumn(name = "id_consulta_medica_integral", referencedColumnName = "idConsultaAtencionIntegral")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Consulta Médica Integral asociada a la orden")
    private ConsultaAtencionIntegral consultaAtencionIntegral;
}