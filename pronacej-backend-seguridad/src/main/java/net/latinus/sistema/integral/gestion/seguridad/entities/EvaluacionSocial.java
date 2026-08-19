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

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;

@Entity
@Data
@Table(name = "ia_evaluacion_social")
@EqualsAndHashCode(of = {"idEvaluacionSocial"},callSuper = true)
public class EvaluacionSocial extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idEvaluacionSocial;
    
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;
    
    @JoinColumn(name = "zona_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("zona vivienda")
    private Catalogo zonaVivienda;
    
    @JoinColumn(name = "sub_zona", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("zona vivienda")
    private Catalogo subZona;
    
    @JoinColumn(name = "material_pared_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("material pared vivienda")
    private Catalogo materialParedVivienda;
    
    @JoinColumn(name = "material_piso_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("material piso vivienda")
    private Catalogo materialPisoVivienda;
    
    @JoinColumn(name = "material_techo_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("material techi vivienda")
    private Catalogo materialTechoVivienda;
    
    @JoinColumn(name = "abastecimiento_agua_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("abastecimiento agua vivienda")
    private Catalogo abastecimientoAguaVivienda;
    
    @JoinColumn(name = "tipo_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tipo vivienda")
    private Catalogo tipoVivienda;
    
    @JoinColumn(name = "tipo_alumbrado_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tipo alumbrado vivienda")
    private Catalogo tipoAlumbradoVivienda;
    
    @JoinColumn(name = "combustible_cocinar_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Combustible cocinar vivienda")
    private Catalogo combustibleCocinarVivienda;
    
    @JoinColumn(name = "tipo_desague_vivienda", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tipo desague vivienda")
    private Catalogo tipoDesagueVivienda;
    
    @JoinColumn(name = "tenencia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("tenencia")
    private Catalogo tenencia;
    
    @JoinColumn(name = "otros_servicios", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("otros servicios")
    private Catalogo otrosServicios;

    @Comment("numero ambientes")
    private Integer numeroAmbientes;

    @Comment("numero ocupantes")
    private Integer numeroOcupantes;

    @Comment("numero habitaciones")
    private Integer numeroHabitaciones;

    @Comment("numero dormitorios")
    private Integer numeroDormitorios;

    @Column(columnDefinition = "TEXT")
    @Comment("grupo amical")
    private String grupoAmical;

    @Column(columnDefinition = "TEXT")
    @Comment("factor riesgo medio")
    private String factorRiesgoMedio;

    @Column(columnDefinition = "TEXT")
    @Comment("area academico laboral")
    private String areaAcademicoLaboral;
    
    @Column(columnDefinition = "TEXT")
    @Comment("areaa social recreacional")
    private String areaSocialRecreacional;
    
    @Column(columnDefinition = "TEXT")
    @Comment("area familiar pareja")
    private String areaFamiliarPareja;
    
    @Column(columnDefinition = "TEXT")
    @Comment("area personal")
    private String areaPersonal;

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