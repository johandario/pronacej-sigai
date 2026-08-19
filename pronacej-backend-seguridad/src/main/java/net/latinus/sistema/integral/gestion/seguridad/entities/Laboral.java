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
@Table(name = "ia_laboral")
@EqualsAndHashCode(of = {"idLaboral"}, callSuper = true)
public class Laboral extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idLaboral;
    @Comment("id de la ficha de identificacion")
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    private FichaIdentificacion fichaIdentificacion;
    @Column(columnDefinition = "TEXT")
    @Comment("experiencia laboral")
    private String experienciaLaboral;
    @Comment("ocupación laboral")
    @JoinColumn(name = "ocupacion_laboral", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo ocupacionLaboral;
    @Comment("modalidad laboral")
    @JoinColumn(name = "modalidad_laboral", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo modalidadLaboral;
    @Comment("recursos apoyo laboral")
    @JoinColumn(name = "recursos_apoyo_laboral", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo recursosApoyoLaboral;
    @Comment("id del estado")
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
