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
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
@Entity
@Data
@Table(name = "ia_evaluacion_seguimiento_educativo_laboral")
@EqualsAndHashCode(of = {"idEvaluacionSeguimiento"}, callSuper = true)
public class EvaluacionSeguimientoEducativoLaboral extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idEvaluacionSeguimiento;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @Comment("fecha de inicio")
    @Column(columnDefinition= "timestamp")
    private Date fechaInicio;
    
    @Comment("fecha fin")
    @Column(columnDefinition= "timestamp")
    private Date fechaFin;
    
    @JoinColumn(name = "tipo_evaluacion_seguimiento", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tipo de evaluacion y seguimiento")
    private Catalogo tipoEvaluacionSeguimiento;
    
    @JoinColumn(name = "id_institucion", referencedColumnName = "idRegistroInstitucion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("institucion educativa o laboral")
    private RegistroInstitucion institucionEducativaLaboral;
    
    @Column(columnDefinition = "TEXT")
    @Comment("tipo de entidad")
    private String tipoEntidad;
    
    @Column(columnDefinition = "TEXT")
    @Comment("direccion")
    private String direccion;
    
    @JoinColumn(name = "medio_verificacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("medio de verificacion")
    private Catalogo medioVerificacion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("resultado del seguimiento")
    private String resultadoSeguimiento;
    @Comment("estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;
    @Comment("id empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    @Column(columnDefinition = "TEXT")
    @Comment("nombre de la institucion en caso de que no esté registrada")
    private String nombreInstitucionOtros;
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}