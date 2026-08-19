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
import java.util.Date;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
@Entity
@Data
@Table(name = "ia_recomendacion_comentario_eval_segu")
@EqualsAndHashCode(of = {"idRecomendacionComentario"}, callSuper = true)
public class RecomendacionComentarioPorEvalSegu extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idRecomendacionComentario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion_seguimiento", referencedColumnName = "idEvaluacionSeguimiento")
    @Comment("evaluacion y seguimiento")
    private EvaluacionSeguimientoEducativoLaboral evaluacionSeguimiento;
    
    @Column(columnDefinition = "TEXT")
    @Comment("comentario")
    private String comentario;
    
    @Comment("fecha")
    @Column(columnDefinition= "timestamp")
    private Date fecha;
    
    @Comment("estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;
    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}