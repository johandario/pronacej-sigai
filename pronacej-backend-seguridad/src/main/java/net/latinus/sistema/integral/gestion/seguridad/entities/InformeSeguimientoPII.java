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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.List;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Entity
@Data
@Table(name = "ia_informe_seguimiento_pii")
@EqualsAndHashCode(of = {"idInformeSeguimientoPII"}, callSuper = true)
public class InformeSeguimientoPII extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idInformeSeguimientoPII;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @JoinColumn(name = "id_informe_tecnico", referencedColumnName = "idInformeTecnicoSustentatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id del informe técnico sustentatorio")
    private InformeTecnicoSustentatorio informeTecnico;
    
    @Column(columnDefinition = "TEXT")
    @Comment("motivo de ingreso")
    private String motivoIngreso;
    
    @Comment("antecedentes psiquiátricos y/o organicidad")
    @Column(columnDefinition = "TEXT")
    private String antecedentesOrganicidad;
    
    @Comment("técnicas utilizadas")
    @Column(columnDefinition = "TEXT")
    private String tecnicasUtilizadas;
    
    @Comment("observación conductual")
    @Column(columnDefinition = "TEXT")
    private String observacionConductual;
    
    @Comment("evaluación del plan psicológico")
    @Column(columnDefinition = "TEXT")
    private String evaluacionPlanPsicologica;
    
    @Comment("evaluación del plan social")
    @Column(columnDefinition = "TEXT")
    private String evaluacionPlanSocial;
    
    @Comment("evaluación del plan conductual")
    @Column(columnDefinition = "TEXT")
    private String evaluacionPlanConductual;
    
    @Comment("evaluación del plan familiar")
    @Column(columnDefinition = "TEXT")
    private String evaluacionPlanFamiliar;
    
    @Comment("evaluación del plan educativo")
    @Column(columnDefinition = "TEXT")
    private String evaluacionPlanEducativa;
    
    @Comment("evaluación del plan laboral")
    @Column(columnDefinition = "TEXT")
    private String evaluacionPlanLaboral;
    
    @Comment("nivel de riesgo")
    @JoinColumn(name = "id_nivel_riesgo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo nivelRiesgo;
    
    @Comment("conclusiones")
    @Column(columnDefinition = "TEXT")
    private String conclusiones;
    
    @Comment("recomendaciones")
    @Column(columnDefinition = "TEXT")
    private String recomendaciones;
    
    @OneToMany(mappedBy = "informeSeguimientoPII", fetch = FetchType.LAZY)
    private List<InstrumentoEvaluacion> instrumentosAplicados;
    
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