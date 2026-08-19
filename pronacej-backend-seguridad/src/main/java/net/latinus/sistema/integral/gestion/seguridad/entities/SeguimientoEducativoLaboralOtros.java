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
import java.util.Date;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
@Entity
@Data
@Table(name = "ia_seguimiento_educativo_laboral_otros")
@EqualsAndHashCode(of = {"idSeguimientoEducativoLaboral"}, callSuper = true)
public class SeguimientoEducativoLaboralOtros extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idSeguimientoEducativoLaboral;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @Comment("id de la ficha de identificación")
    private FichaIdentificacion fichaIdentificacion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("institución visitada")
    private String institucionVisitada;
    
    @Column(columnDefinition = "TEXT")
    @Comment("persona entrevistada")
    private String personaEntrevistada;
    
    @Column(columnDefinition = "TEXT")
    @Comment("dirección")
    private String direccion;
    
    @Comment("fecha del seguimiento")
    @Column(columnDefinition= "timestamp")
    private Date fechaSeguimiento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_seguimiento", referencedColumnName = "idCatalogo")
    @Comment("tipo de seguimiento educativo/laboral/otros")
    private Catalogo tipoSeguimiento;
    
    @Column(columnDefinition = "TEXT")
    @Comment("medio de verificación")
    private String medioVerificacion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("resultado del seguimiento")
    private String resultadoSeguimiento;
    
    @Column(columnDefinition = "TEXT")
    @Comment("sugerencias y recomendaciones")
    private String sugerenciasRecomendaciones;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_programa", referencedColumnName = "idJerarquia")
    @Comment("programa")
    private Jerarquia programa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ambiente", referencedColumnName = "idJerarquia")
    @Comment("ambiente")
    private Jerarquia ambiente;
    
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
