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

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
@Entity
@Data
@Table(name = "ia_situacion_educativa_laboral_ocio")
@EqualsAndHashCode(of = {"idSituacionEducativaLaboralOcio"}, callSuper = true)
public class SituacionEducativaLaboralOcio extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idSituacionEducativaLaboralOcio;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("centro de estudios")
    private String centroEstudios;
    
    @JoinColumn(name = "situacion_educativa", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id situacion educativa")
    private Catalogo situacionEducativa;
    
    @JoinColumn(name = "rendimiento_educativo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id rendimiento educativo")
    private Catalogo rendimientoEducativo;
    
    @JoinColumn(name = "modalidad_educativa", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id modalidad educativa")
    private Catalogo modalidadEducativa;
    @JoinColumn(name = "modalidad_estudio", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id modalidad estudio")
    private Catalogo modalidadEstudio;
    
    
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id estado")
    private Catalogo estado;
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id empresa")
    private Empresa empresa;
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
