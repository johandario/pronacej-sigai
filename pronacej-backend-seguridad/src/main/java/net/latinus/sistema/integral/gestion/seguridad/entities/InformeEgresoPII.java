package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.text.SimpleDateFormat;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Entity
@Data
@Table(name = "ia_informe_egreso_pii")
@EqualsAndHashCode(of = {"idInformeEgresoPII"}, callSuper = true)
public class InformeEgresoPII extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idInformeEgresoPII;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @JoinColumn(name = "id_informe_seguimiento", referencedColumnName = "idInformeSeguimientoPII")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id del informe de seguimiento PII")
    private InformeSeguimientoPII informeSeguimiento;
    
    @Comment("motivo de ingreso al PII")
    @Column(columnDefinition = "TEXT")
    private String motivoIngresoPII;
    
    @Comment("descripcion psicologica del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionPsicologicaPlanTratamiento;
    
    @Comment("descripcion social del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionSocialPlanTratamiento;
    
    @Comment("descripcion conductual del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionConductualPlanTratamiento;
    
    @Comment("descripcion familiar del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionFamiliarPlanTratamiento;
    
    @Comment("descripcion del nivel de riesgo del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionNivelRiesgoPlanTratamiento;
    
    @Comment("descripcion de evolucion psicologica del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionEvolucionPsicologicaPlanTratamiento;
    
    @Comment("descripcion de evolucion social del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionEvolucionSocialPlanTratamiento;
    
    @Comment("descripcion de evolucion conductual del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionEvolucionConductualPlanTratamiento;
    
    @Comment("descripcion de evolucion familiar del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionEvolucionFamiliarPlanTratamiento;
    
    @Comment("descripcion de evolucion del nivel de riesgo del plan de tratamiento")
    @Column(columnDefinition = "TEXT")
    private String descripcionEvolucionNivelRiesgoPlanTratamiento;
    
    @Comment("conclusiones")
    @Column(columnDefinition = "TEXT")
    private String conclusiones;
    
    @Comment("recomendaciones")
    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(
                    EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
            
            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }
}