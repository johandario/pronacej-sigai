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
@Table(name = "ia_situacion_riesgo_social")
@EqualsAndHashCode(of = {"idSituacionRiesgoSocial"}, callSuper = true)
public class SituacionRiesgoSocial extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idSituacionRiesgoSocial;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("ante de la fami")
    private String anteDeliFami;
    
    @Column(columnDefinition = "TEXT")
    @Comment("prim mani infor adol")
    private String primManiInfrAdol;
    
    @Comment("evasion hogar")
    private Boolean evasionHogar;
    
    @Column(columnDefinition = "TEXT")
    @Comment("estado salud general")
    private String estadoSaludGeneral;
    
    @Column(columnDefinition = "TEXT")
    @Comment("problemas legales")
    private String problemasLegales;
    
    @Column(columnDefinition = "TEXT")
    @Comment("observaciones")
    private String observaciones;
    
    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}