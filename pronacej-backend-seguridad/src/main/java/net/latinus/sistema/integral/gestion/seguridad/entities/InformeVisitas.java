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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;

@Entity
@Data
@Table(name = "ia_informe_visitas")
@EqualsAndHashCode(of = {"idInformeVisitas"}, callSuper = true)
public class InformeVisitas extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idInformeVisitas;
    
    @JoinColumn(name = "id_persona_relacionada", referencedColumnName = "idPersonasRelacionadas")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la persona relacionada")
    private PersonaRelacionada personaRelacionada;
    
    @Comment("tipo de autorización")
    @JoinColumn(name = "id_tipo_autorizacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoAutorizacion;
    
    @Temporal(TemporalType.DATE)
    @Comment("fecha de inicio de la autorización")
    private Date fechaInicio;
    
    @Temporal(TemporalType.DATE)
    @Comment("fecha de fin de la autorización")
    private Date fechaFin;
    
    @Column(columnDefinition = "TEXT")
    @Comment("causales de restricción")
    private String causalesRestriccion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("observaciones de la visita")
    private String observaciones;
    
    @Column(columnDefinition = "TEXT")
    @Comment("id de la ficha principal relacionada")
    private String tokenIdentificadorFichaPrincipal;
    
    @Comment("id empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
