package net.latinus.sistema.integral.gestion.seguridad.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
@Entity
@Data
@Table(name = "ia_areas_situacion_educativa_laboral_ocio")
@EqualsAndHashCode(of = {"idAreasSituacionEducativaLaboralOcio"}, callSuper = true)
public class AreasSituacionEducativaLaboralOcio extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAreasSituacionEducativaLaboralOcio;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    private FichaIdentificacion fichaIdentificacion;
    
    @Column(columnDefinition = "TEXT")
    private String actitudEstudios;
    
    @Column(columnDefinition = "TEXT")
    private String desarrolloEducativo;
    
    @Column(columnDefinition = "TEXT")
    private String interesesVocacionales;
    
    @Column(columnDefinition = "TEXT")
    private String observacionesEducativas;
    
    @Column(columnDefinition = "TEXT")
    private String actitudEmpleo;
    
    @Column(columnDefinition = "TEXT")
    private String capacitacionesEmpleabilidad;
    
    @Column(columnDefinition = "TEXT")
    private String observacionesLaborales;
    
    @Column(columnDefinition = "TEXT")
    private String pasatiempos;
    
    @Column(columnDefinition = "TEXT")
    private String talentos;
    
    @Column(columnDefinition = "TEXT")
    private String participacionGrupal;
    
    @Column(columnDefinition = "TEXT")
    private String usoTiempo;
    
    @Column(columnDefinition = "TEXT")
    private String observacionesOcio;
    
    @Comment("estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;
    @Comment("id empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
