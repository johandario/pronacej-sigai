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
@Table(name = "ia_orientacion_consejeria_familiar")
@EqualsAndHashCode(of = {"idOrientacionConsejeriaFamiliar"}, callSuper = true)
public class OrientacionConsejeriaFamiliar extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idOrientacionConsejeriaFamiliar;
    
    @JoinColumn(name = "id_persona_relacionada", referencedColumnName = "idPersonasRelacionadas")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la persona relacionada")
    private PersonaRelacionada personaRelacionada;
    
    @Column(columnDefinition = "timestamp")
    @Comment("fecha de la orientación/consejería")
    private Date fecha;
    
    @Column(columnDefinition = "TEXT")
    @Comment("descripción de la orientación/consejería")
    private String descripcion;
    
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