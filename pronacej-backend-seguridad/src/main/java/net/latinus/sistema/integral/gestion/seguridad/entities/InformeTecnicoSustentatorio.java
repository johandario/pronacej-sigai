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
@Table(name = "ia_informe_tecnico_sustentatorio")
@EqualsAndHashCode(of = {"idInformeTecnicoSustentatorio"}, callSuper = true)
public class InformeTecnicoSustentatorio extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idInformeTecnicoSustentatorio;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("motivo")
    private String motivo;
    
    @Column(columnDefinition = "TEXT")
    @Comment("criterios de seleccion")
    private String criteriosSeleccion;
    
    @Comment("analisis psicologico")
    @Column(columnDefinition = "TEXT")
    private String analisisPsicologico;
    
    @Comment("analisis social")
    @Column(columnDefinition = "TEXT")
    private String analisisSocial;
    
    @Comment("analisis conductual")
    @Column(columnDefinition = "TEXT")
    private String analisisConductual;
    
    @Comment("analisis familiar")
    @Column(columnDefinition = "TEXT")
    private String analisisFamiliar;
    
    @Comment("propuesta de actividad formativa")
    @Column(columnDefinition = "TEXT")
    private String propuestaActividadFormativa;
    
    @Comment("importancia de la participacion del adolescente")
    @Column(columnDefinition = "TEXT")
    private String importanciaParticipacionAdolescente;
    
    @Comment("objetivos a conseguir")
    @Column(columnDefinition = "TEXT")
    private String objetivosConseguir;
    
    @Comment("duracion en horas")
    private Float duracion;
    
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