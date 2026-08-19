package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "par_plantilla_formulario")
@EqualsAndHashCode(of = {"idPlantillaFormulario"}, callSuper = true)
@Comment("Tabla de formularios template")
public class PlantillaFormulario extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de formularios template")
    private Long idPlantillaFormulario;
    
    @Comment("Catalogo con información de la pantalla formulario relacionado a la plantilla")
    @JoinColumn(name = "formulario_relacionado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo formularioRelacionado;

    @Comment("Nemonico de formularios template")
    private String nemonico;

    @Comment("Cuerpo del html sin la parte principal y final para poder editar")
    @Column(columnDefinition = "TEXT")
    private String contenidoHtml;

    @Comment("Cuerpo del formulario template")
    @Column(columnDefinition = "TEXT")
    private String formularioString;

    @Comment("Razon del formulario template")
    private String razon;

    @Comment("Descipcion del formulario template")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Comment("id del estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;
    
    @Comment("Id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
