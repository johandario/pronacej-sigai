package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ClasificacionEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(
        name = "par_clasificacion_enfermedad",
        indexes = {
                @Index(name = "idx_par_clasificacion_enfermedad_codigo", columnList = "codigo"),
                @Index(name = "idx_par_clasificacion_enfermedad_nombre", columnList = "nombre")
        }
)
@EqualsAndHashCode(of = {"idClasificacionEnfermedad"}, callSuper = true)
@Comment("Tabla de catalogo")
public class ClasificacionEnfermedad extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idClasificacionEnfermedad;

    @Comment("nombre de la enfermedad")
    private String nombre;

    @Comment("nemonico del catalago")
    private String codigo;

    @Comment("Descripción de la enfermedad")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Comment("Pertenece a hombres")
    private Boolean aplicaHombre;

    @Comment("Pertenece a mujeres")
    private Boolean aplicaMujer;

    public ClasificacionEnfermedadDTO convertirADTO() {
        ClasificacionEnfermedadDTO objetoDTO = new ClasificacionEnfermedadDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        objetoDTO.setCodigo(this.getCodigo());
        objetoDTO.setNombre(this.getNombre());
        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}