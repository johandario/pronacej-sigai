package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;

@Entity
@Data
@Table(name = "ia_suspension_visitas")
@EqualsAndHashCode(of = {"idSuspensionVisitas"}, callSuper = true)
public class SuspensionVisitas extends EntidadBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idSuspensionVisitas;
    
    @OneToMany(mappedBy = "suspensionVisitas", fetch = FetchType.LAZY)
    private List<CometimientoInfraccion> cometimientosInfraccion = new ArrayList<>();
    
    @Temporal(TemporalType.DATE)
    @Comment("fecha de inicio de la suspensión")
    private Date fechaInicio;
    
    @Temporal(TemporalType.DATE)
    @Comment("fecha de fin de la suspensión")
    private Date fechaFin;
    
    @Column(columnDefinition = "TEXT")
    @Comment("número de oficio de sanción")
    private String oficioDeSancion;
    
    @Column(columnDefinition = "TEXT")
    @Comment("observaciones de la suspensión")
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
